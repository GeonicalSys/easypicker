package com.keenfin.easypicker;

import android.app.Application;
import android.content.Intent;
import android.view.View;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.android.controller.ServiceController;
import org.robolectric.annotation.Config;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.junit.Assert.*;

/** Owning-library service/HTTP-copy path with an in-memory HTTP boundary, never a socket. */
@RunWith(RobolectricTestRunner.class)
@Config(application = Application.class, sdk = 28)
public class AttachmentDownloadFailureStateTest {
    private static final String URL = "https://attachment-test.invalid/resource/1/attachment/2";
    private static final String OTHER_URL = "https://attachment-test.invalid/unrelated";
    private static final String NAME = "technical-attachment.bin";
    private static final byte[] PAYLOAD = "complete technical transfer".getBytes(StandardCharsets.UTF_8);
    private static final byte[] OLD_CACHE = "previous complete cache".getBytes(StandardCharsets.UTF_8);
    private Application application;
    private ServiceController<RecordedService> controller;
    private RecordedService service;
    private Path directory;
    private String relativePath;
    private final List<ScriptedConnection> requests = new ArrayList<>();

    @Before public void prepare() throws Exception {
        application = RuntimeEnvironment.getApplication();
        Path cache = application.getExternalCacheDir().toPath();
        Files.createDirectories(cache);
        directory = Files.createTempDirectory(cache, "attachment-regression-");
        relativePath = directory.getFileName() + "/";
        controller = Robolectric.buildService(RecordedService.class).create();
        service = controller.get();
        assertFalse(DownloadPhotoIntentService.getProgressList().contains(URL));
    }

    @After public void finishOwnedResources() throws Exception {
        // Release before joining even when an assertion fails while the body is blocked.
        for (ScriptedConnection request : requests) request.body.release.countDown();
        for (ScriptedConnection request : requests) {
            Thread worker = request.worker.get();
            if (worker != null) {
                worker.join(10000);
                assertFalse("Owned download worker must stop before deleting its files", worker.isAlive());
            }
        }
        // The RED source leaks output streams on failure. This fixture owns/captures only
        // its own streams and closes them after the worker, without masking the oracle.
        if (service != null) for (OutputStream output : service.outputs) output.close();
        for (ScriptedConnection request : requests) request.body.close();
        DownloadPhotoIntentService.deleteProgressListItem(URL);
        DownloadPhotoIntentService.deleteProgressListItem(OTHER_URL);
        if (controller != null) controller.destroy();
        if (directory != null) {
            try (Stream<Path> paths = Files.walk(directory)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) Files.delete(path);
            }
        }
    }

    @Test public void sole404EndsWorkAndActualPickerOffersRetryWithoutCreatingCache() throws Exception {
        PhotoPicker picker = picker();
        picker.urlIsInProgress.add(URL);
        ScriptedConnection request = request(HttpURLConnection.HTTP_NOT_FOUND, new Body(new byte[0], false, null));
        start(request, false);
        awaitStopped(request);
        List<Intent> events = events();
        assertEquals(Arrays.asList("start", "end"), eventTypes(events));
        assertEquals(404, events.get(1).getIntExtra(DownloadPhotoIntentService.EXTRA_HTTP_CODE, -2));
        assertFalse(events.get(1).getBooleanExtra(DownloadPhotoIntentService.EXTRA_PREVIEW, true));
        assertFalse(DownloadPhotoIntentService.getProgressList().contains(URL));
        for (Intent event : events) picker.updateStatus(event);
        assertFalse(picker.urlIsInProgress.contains(URL));
        assertFalse(Files.exists(target()));
        assertOnlyFiles();
    }

    @Test public void failedPositivePrefixIsNeverAReadyCacheDuringOrAfterTransfer() throws Exception {
        Body body = new Body(PAYLOAD, true, new IOException("technical transfer failure"));
        ScriptedConnection request = request(200, body);
        start(request, false);
        assertTrue("The actual copy reached the second read", body.waiting.await(10, TimeUnit.SECONDS));
        assertFalse("Incomplete bytes cannot occupy the final cache name", Files.exists(target()));
        body.release.countDown();
        awaitStopped(request);
        assertTerminal(-1, false);
        assertFalse(Files.exists(target()));
        assertOnlyFiles();
        assertPickerRetryVisible();
    }

    @Test public void failedReplacementPreservesWholePreviousCacheAndSibling() throws Exception {
        Files.write(target(), OLD_CACHE);
        Path sibling = directory.resolve("unrelated-cache.bin");
        Files.write(sibling, new byte[]{9, 8, 7});
        ScriptedConnection request = request(200, new Body(PAYLOAD, false, new IOException("technical body failure")));
        start(request, false);
        awaitStopped(request);
        assertTerminal(-1, false);
        assertArrayEquals(OLD_CACHE, Files.readAllBytes(target()));
        assertArrayEquals(new byte[]{9, 8, 7}, Files.readAllBytes(sibling));
        assertOnlyFiles(NAME, "unrelated-cache.bin");
    }

    @Test public void successPublishesExactBytesOnlyAfterCompletedBodyAndClosedOutput() throws Exception {
        Body body = new Body(PAYLOAD, true, null);
        ScriptedConnection request = request(200, body);
        start(request, false);
        assertTrue(body.waiting.await(10, TimeUnit.SECONDS));
        assertFalse("A positive partial transfer is not ready", Files.exists(target()));
        body.release.countDown();
        awaitStopped(request);
        assertTerminal(200, false);
        assertArrayEquals(PAYLOAD, Files.readAllBytes(target()));
        assertOnlyFiles(NAME);
        assertThrows(IOException.class, () -> service.outputs.get(0).write(1));
        assertFalse(DownloadPhotoIntentService.getProgressList().contains(URL));
    }

    @Test public void freshSameUrlRetrySucceedsAfterFailureWithoutStaleWorkOrPartialBytes() throws Exception {
        ScriptedConnection first = request(200, new Body(new byte[]{1, 2, 3}, false, new IOException("technical first attempt")));
        start(first, false);
        awaitStopped(first);
        assertTerminal(-1, false);
        assertFalse(Files.exists(target()));
        assertFalse(DownloadPhotoIntentService.getProgressList().contains(URL));
        ScriptedConnection second = request(200, new Body(PAYLOAD, false, null));
        start(second, false);
        awaitStopped(second);
        List<Intent> events = events();
        assertEquals(Arrays.asList("start", "end", "start", "end"), eventTypes(events));
        assertEquals(200, events.get(3).getIntExtra(DownloadPhotoIntentService.EXTRA_HTTP_CODE, -2));
        assertArrayEquals(PAYLOAD, Files.readAllBytes(target()));
        assertOnlyFiles(NAME);
        assertFalse(DownloadPhotoIntentService.getProgressList().contains(URL));
    }

    @Test public void preview404AndInterruptedOrServerFailureKeepTheirOwnStatusAndOtherWork() throws Exception {
        DownloadPhotoIntentService.putProgressListItem(OTHER_URL);
        ScriptedConnection preview = request(404, new Body(new byte[0], false, null));
        start(preview, true);
        awaitStopped(preview);
        assertEquals(Collections.singletonList("end"), eventTypes(events()));
        assertTerminal(404, true);
        assertFalse(Files.exists(target()));
        ScriptedConnection interrupted = request(200, new Body(PAYLOAD, false, new InterruptedIOException("technical interrupted copy")));
        start(interrupted, false);
        awaitStopped(interrupted);
        assertTerminal(-1, false);
        assertFalse(Files.exists(target()));
        ScriptedConnection refused = request(503, new Body(new byte[0], false, null));
        start(refused, false);
        awaitStopped(refused);
        assertTerminal(-1, false);
        assertTrue(DownloadPhotoIntentService.getProgressList().contains(OTHER_URL));
        assertOnlyFiles();
    }

    @Test public void closeOrPublicationFailureNeverPublishesPartialOrDeletesOtherEntries() throws Exception {
        service.failOutputClose = true;
        ScriptedConnection closeFailure = request(200, new Body(PAYLOAD, false, null));
        start(closeFailure, false);
        awaitStopped(closeFailure);
        assertTerminal(-1, false);
        assertFalse(Files.exists(target()));
        assertOnlyFiles();
        service.failOutputClose = false;
        Files.createDirectory(target());
        Files.write(target().resolve("owned-destination-guard"), OLD_CACHE);
        ScriptedConnection publicationFailure = request(200, new Body(PAYLOAD, false, null));
        start(publicationFailure, false);
        awaitStopped(publicationFailure);
        assertTerminal(-1, false);
        assertArrayEquals(OLD_CACHE, Files.readAllBytes(target().resolve("owned-destination-guard")));
        assertOnlyFiles(NAME);
    }

    private Path target() { return directory.resolve(NAME); }

    private ScriptedConnection request(int status, Body body) throws Exception {
        ScriptedConnection connection = new ScriptedConnection(status, body);
        requests.add(connection);
        return connection;
    }

    private void start(ScriptedConnection request, boolean preview) {
        service.connection = request;
        if (preview) DownloadPhotoIntentService.startActionDownload(application, URL, relativePath, NAME,
                null, null, 64, 64, URL, "technical-unit-test");
        else DownloadPhotoIntentService.startActionDownload(application, URL, relativePath, NAME,
                null, null, "technical-unit-test");
        Intent intent = Shadows.shadowOf(application).getNextStartedService();
        assertNotNull("Use the actual public startActionDownload intent", intent);
        service.onStartCommand(intent, 0, requests.size());
    }

    private void awaitStopped(ScriptedConnection request) throws Exception {
        assertTrue("The actual service worker reached the controlled HTTP boundary", request.entered.await(10, TimeUnit.SECONDS));
        Thread worker = request.worker.get();
        assertNotNull(worker);
        worker.join(10000);
        assertFalse("The actual owned worker stopped", worker.isAlive());
    }

    private List<Intent> events() {
        List<Intent> result = new ArrayList<>();
        for (Intent intent : Shadows.shadowOf(application).getBroadcastIntents())
            if (DownloadPhotoIntentService.DOWNLOAD_ACTION.equals(intent.getAction())
                    && URL.equals(intent.getStringExtra(DownloadPhotoIntentService.EXTRA_URL_ID))) result.add(intent);
        return result;
    }

    private List<String> eventTypes(List<Intent> events) {
        List<String> result = new ArrayList<>();
        for (Intent event : events) result.add(event.getStringExtra(DownloadPhotoIntentService.EXTRA_TYPE));
        return result;
    }

    private void assertTerminal(int status, boolean preview) {
        List<Intent> all = events();
        assertFalse("Terminal broadcast exists", all.isEmpty());
        Intent terminal = all.get(all.size() - 1);
        assertEquals("end", terminal.getStringExtra(DownloadPhotoIntentService.EXTRA_TYPE));
        assertEquals(status, terminal.getIntExtra(DownloadPhotoIntentService.EXTRA_HTTP_CODE, -2));
        assertEquals(preview, terminal.getBooleanExtra(DownloadPhotoIntentService.EXTRA_PREVIEW, !preview));
        assertEquals(URL, terminal.getStringExtra(DownloadPhotoIntentService.EXTRA_URL_ID));
    }

    private void assertOnlyFiles(String... names) throws Exception {
        try (Stream<Path> paths = Files.list(directory)) {
            List<String> actual = paths.map(p -> p.getFileName().toString()).sorted().toList();
            List<String> expected = new ArrayList<>(Arrays.asList(names));
            Collections.sort(expected);
            assertEquals("No leftover request stage or unrelated deletion", expected, actual);
        }
    }

    private PhotoPicker picker() {
        PhotoPicker picker = new PhotoPicker(application, true, null, null);
        picker.setLayoutParams(new android.widget.FrameLayout.LayoutParams(400, 400));
        return picker;
    }

    private void assertPickerRetryVisible() throws Exception {
        PhotoPicker picker = picker();
        AttachInfo info = new AttachInfo(true, URL, relativePath, NAME, "technical attachment", null, "2");
        picker.restoreImages(new ArrayList<>(), Collections.singletonMap("2", info));
        // Prevent preview transport/decoding in this control-binding test, without seeding
        // the final file whose readiness is the oracle.
        Files.write(directory.resolve("preview_" + NAME), new byte[]{1});
        PhotoPicker.PhotoAdapter adapter = (PhotoPicker.PhotoAdapter) picker.getAdapter();
        PhotoViewHolder original = adapter.onCreateViewHolder(picker, 0);
        PhotoViewHolder holder = new PhotoViewHolder(original.itemView) {
            @Override public void loadPhoto(android.content.Context context, String path, int size, java.io.File preview) {
                // Bitmap decoding is outside the visibility/transfer contract.
            }
        };
        adapter.onBindViewHolder(holder, 0);
        assertEquals(View.GONE, holder.progressBar.getVisibility());
        assertEquals(View.VISIBLE, holder.mPhotoDownload.getVisibility());
        Files.delete(directory.resolve("preview_" + NAME));
    }

    public static class RecordedService extends DownloadPhotoIntentService {
        ScriptedConnection connection;
        boolean failOutputClose;
        final List<OutputStream> outputs = new ArrayList<>();

        @Override public HttpURLConnection getHttpConnection(String method, String url, String username,
                                                             String password, String userAgent) {
            ScriptedConnection current = connection;
            current.worker.set(Thread.currentThread());
            current.entered.countDown();
            return current;
        }

        @Override public int getStream(String url, String username, String password, OutputStream output,
                                       String userAgent) throws IOException {
            outputs.add(output);
            OutputStream boundary = failOutputClose ? new FilterOutputStream(output) {
                @Override public void close() throws IOException {
                    super.close();
                    throw new IOException("technical output close failure");
                }
            } : output;
            return super.getStream(url, username, password, boundary, userAgent);
        }
    }

    private static final class ScriptedConnection extends HttpURLConnection {
        final int status;
        final Body body;
        final AtomicReference<Thread> worker = new AtomicReference<>();
        final CountDownLatch entered = new CountDownLatch(1);
        ScriptedConnection(int status, Body body) throws Exception {
            super(new URL(URL));
            this.status = status;
            this.body = body;
        }
        @Override public int getResponseCode() { return status; }
        @Override public InputStream getInputStream() { return body; }
        @Override public void disconnect() { }
        @Override public boolean usingProxy() { return false; }
        @Override public void connect() { throw new AssertionError("No socket is permitted"); }
    }

    private static final class Body extends InputStream {
        final byte[] bytes;
        final boolean pauseAfterPrefix;
        final IOException failure;
        final CountDownLatch waiting = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        int position;
        Body(byte[] bytes, boolean pauseAfterPrefix, IOException failure) {
            this.bytes = bytes;
            this.pauseAfterPrefix = pauseAfterPrefix;
            this.failure = failure;
        }
        @Override public int read(byte[] buffer, int offset, int length) throws IOException {
            if (position == 0 && bytes.length > 0) {
                int count = Math.min(bytes.length, length);
                System.arraycopy(bytes, 0, buffer, offset, count);
                position += count;
                return count;
            }
            if (pauseAfterPrefix) {
                waiting.countDown();
                try {
                    if (!release.await(10, TimeUnit.SECONDS)) throw new IOException("Technical test gate expired");
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw new InterruptedIOException("Technical test gate interrupted");
                }
            }
            if (failure != null) throw failure;
            return -1;
        }
        @Override public int read() throws IOException { throw new AssertionError("Actual copy uses a bounded byte buffer"); }
    }
}
