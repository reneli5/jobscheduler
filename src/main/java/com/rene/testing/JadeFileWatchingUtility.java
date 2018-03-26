package com.rene.testing;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.*;

public class JadeFileWatchingUtility implements Runnable{

    private static final Logger LOGGER = Logger.getLogger(JadeFileWatchingUtility.class);
    private final WatchService objWatchService;
    private final Map<WatchKey, Path> mapWatchKeys;
    private boolean recursive = false;
    private boolean flgTraceIsActive = false;
    private Path strFolderName2Watch4 = null;
    private LocalDateTime startDate;
    private FileWatcherOperationJob watcherOperationJob;

    public JadeFileWatchingUtility(final Path pstrFolderName2Watch, final boolean pflgRecursive, FileWatcherOperationJob watcherOperationJob) throws IOException {
        LOGGER.info("********** constructor started ******");
        this.watcherOperationJob = watcherOperationJob;
        startDate = LocalDateTime.now();
        objWatchService = FileSystems.getDefault().newWatchService();
        mapWatchKeys = new HashMap<>();
        recursive = pflgRecursive;
        strFolderName2Watch4 = pstrFolderName2Watch;
        if (recursive) {
            LOGGER.debug(String.format("Start recursive watching for %s ...\n", pstrFolderName2Watch.getFileName()));
            registerAll(pstrFolderName2Watch);
        } else {
            LOGGER.debug(String.format("Start non-recursive watching for %s ...\n", pstrFolderName2Watch.getFileName()));
            register(pstrFolderName2Watch);
        }
        flgTraceIsActive = true;
        LOGGER.info("********** constructor ok ******");
    }

    static <T> WatchEvent<T> cast(final WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }

    public void processEvents() {
        for(;;) {
            LOGGER.info("********** inside loop started 1 ******");
            WatchKey key;
            try {
                key = objWatchService.take();
            } catch (InterruptedException x) {
                x.printStackTrace();
                LOGGER.info(String.format("FileWatcher interrupted for Folder '%1$s'", strFolderName2Watch4.getFileName()));
                return;
            }
            LOGGER.info("********** inside loop started 2 ******");
            Path dir = mapWatchKeys.get(key);
            LOGGER.info("********** inside loop started 2 ******" + dir.getFileName().toString());
            if (dir == null) {
                LOGGER.info("WatchKey not recognized!!");
                continue;
            }
            LOGGER.info("********** inside loop started 3 ******");
            for (WatchEvent<?> event : key.pollEvents()) {
                LOGGER.info("********** inside Event occured 1 ******");
                Kind<?> kind = event.kind();
                if (kind.equals(OVERFLOW)) {
                    LOGGER.info(String.format("Overflow occurred processing Folder %1$s", strFolderName2Watch4));
                    continue;
                }
                LOGGER.info("********** inside Event occured 2 ******");
                WatchEvent<Path> ev = cast(event);
                Path name = ev.context();
                Path child = dir.resolve(name);
                LOGGER.info(String.format("%s: %s\n", event.kind().name(), child));
                if (recursive && kind.equals(ENTRY_CREATE)) {
                    try {
                        if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
                            registerAll(child);
                        }else{
                            LOGGER.info("********** it's a regular file ****** " + child.getFileName().toAbsolutePath());
                            BlockingQueue<String> messageQueue = FileQueueingFactory.getMessageQueue();
                            try {
                                messageQueue.put(child.getFileName().toAbsolutePath().toString());
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            LOGGER.info("********** it's a regular file triggered ****** " + child.getFileName().toAbsolutePath());
                        }
                    } catch (IOException x) {
                        x.printStackTrace();
                        throw new JobSchedulerException(x);
                    }
                }
                LOGGER.info("********** inside Event occured 3 ******");
            }
            LOGGER.info("********** inside loop started 4******");
            boolean valid = key.reset();
            if (!valid) {
                mapWatchKeys.remove(key);
                if (mapWatchKeys.isEmpty()) {
                    break;
                }
            }
        }
    }

    private void registerAll(final Path pstrStartFolderName) throws IOException {
        Files.walkFileTree(pstrStartFolderName, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void register(final Path dir) throws IOException {
        WatchKey key = dir.register(objWatchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        if (flgTraceIsActive) {
            Path prev = mapWatchKeys.get(key);
            if (prev == null) {
                LOGGER.debug(String.format("register: %s\n", dir));
            } else {
                if (!dir.equals(prev)) {
                    LOGGER.debug(String.format("update: %s -> %s\n", prev, dir));
                }
            }
        }
        mapWatchKeys.put(key, dir);
    }

    public void run() {
        this.processEvents();
    }


}
