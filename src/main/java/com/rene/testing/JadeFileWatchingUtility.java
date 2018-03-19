package com.rene.testing;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.concurrent.SOSThreadPoolExecutor;
import sos.scheduler.job.JobSchedulerJobAdapter;

public class JadeFileWatchingUtility implements Runnable{

    private static final Logger LOGGER = Logger.getLogger(JadeFileWatchingUtility.class);
    private final WatchService objWatchService;
    private final Map<WatchKey, Path> mapWatchKeys;
    private boolean recursive = false;
    private boolean flgTraceIsActive = false;
    private Path strFolderName2Watch4 = null;
    private LocalDateTime startDate;

    public JadeFileWatchingUtility(final Path pstrFolderName2Watch, final boolean pflgRecursive) throws IOException {
        LOGGER.info("********** constructor started ******");
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

    static void usage() {
        LOGGER.error("usage: java JadeFileWatchingUtility [-r] dir [dir ...]");
        System.exit(-1);
    }

    public static void main(final String[] args) throws IOException {
        if (args.length == 0) {
            usage();
        }
        boolean flgRecurseSubFolders = false;
        int intNoOfFolders2WatchFor = args.length;
        int dirArg = 0;
        if ("-r".equals(args[0])) {
            intNoOfFolders2WatchFor--;
            if (args.length < 2) {
                usage();
            }
            flgRecurseSubFolders = true;
            dirArg++;
        }
        SOSThreadPoolExecutor mtpe = new SOSThreadPoolExecutor(intNoOfFolders2WatchFor);
        int cpus = Runtime.getRuntime().availableProcessors();
        LOGGER.debug("max avl cpus = " + cpus);
        Future<Runnable>[] objRunningThreads = new Future[intNoOfFolders2WatchFor];
        int intThread = 0;
        for (int i = dirArg; i < args.length; i++) {
            Path dir = Paths.get(args[i]);
            Future<Runnable> objF = mtpe.runTask(new JadeFileWatchingUtility(dir, flgRecurseSubFolders));
            objRunningThreads[intThread++] = objF;
        }
        try {
            Thread.sleep(20000);
            for (int i = 0; i < intNoOfFolders2WatchFor; i++) {
                Future<Runnable> objF = objRunningThreads[i];
                objF.cancel(true);
            }
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
        }
        mtpe.shutDown();
        LOGGER.info("Terminate");
        System.exit(0);
    }

    private boolean isTimeOut(){

        LocalDateTime now = LocalDateTime.now();

        long diff = startDate.until(now, ChronoUnit.MILLIS);

        if(diff < 60000){
            return true;
        }
        return false;
    }

    public void processEvents() {
        while (isTimeOut()) {
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
