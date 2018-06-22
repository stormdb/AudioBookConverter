package uk.yermak.audiobookconverter;

import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.probe.FFmpegFormat;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by yermak on 1/10/2018.
 */
public class MediaLoader {

    private List<String> fileNames;
    private static String FFPROBE = new File("external/x64/ffprobe.exe").getAbsolutePath();
    private static ExecutorService executorService = Executors.newWorkStealingPool();

    public MediaLoader(List<String> files) {
        this.fileNames = files;
        Collections.sort(fileNames);
    }

    public List<MediaInfo> loadMediaInfo() {
        try {
            FFprobe ffprobe = new FFprobe(FFPROBE);
            List<MediaInfo> media = new ArrayList<>();
            for (String fileName : fileNames) {
                Future futureLoad = executorService.submit(new MediaInfoCallable(ffprobe, fileName));
                MediaInfo mediaInfo = new MediaInfoProxy(fileName, futureLoad);
                media.add(mediaInfo);
            }
            return media;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static class MediaInfoCallable implements Callable<MediaInfo> {

        private final String filename;
        private FFprobe ffprobe;

        public MediaInfoCallable(FFprobe ffprobe, String filename) {
            this.ffprobe = ffprobe;
            this.filename = filename;
        }

        @Override
        public MediaInfo call() throws Exception {
            try {
                FFmpegProbeResult probeResult = ffprobe.probe(filename);
                FFmpegFormat format = probeResult.getFormat();
                MediaInfoBean mediaInfo = new MediaInfoBean(filename);

                List<FFmpegStream> streams = probeResult.getStreams();
                for (int i = 0; i < streams.size(); i++) {
                    FFmpegStream fFmpegStream = streams.get(i);
                    if ("mp3".equals(fFmpegStream.codec_name)) {
                        mediaInfo.setChannels(fFmpegStream.channels);
                        mediaInfo.setFrequency(fFmpegStream.sample_rate);
                        mediaInfo.setBitrate((int) fFmpegStream.bit_rate);
                        mediaInfo.setDuration((long) fFmpegStream.duration * 1000);
                    } else if ("mjpeg".equals(fFmpegStream.codec_name)) {
                        Future futureLoad = executorService.submit(new ArtWorkCallable(mediaInfo, "jpg"));
//                        futureLoad.get();
                        ArtWorkProxy artWork = new ArtWorkProxy(futureLoad, "jpg");
                        mediaInfo.setArtWork(artWork);
                    }
                }
                AudioBookInfo bookInfo = new AudioBookInfo(format.tags);
                mediaInfo.setBookInfo(bookInfo);
                return mediaInfo;
            } catch (IOException e) {
                e.printStackTrace();
                throw e;
            }
        }
    }

    private static class ArtWorkCallable implements Callable<ArtWork> {

        private static final String FFMPEG = new File("external/x64/ffmpeg.exe").getAbsolutePath();

        private MediaInfoBean mediaInfo;
        private String format;

        public ArtWorkCallable(MediaInfoBean mediaInfo, String format) {
            this.mediaInfo = mediaInfo;
            this.format = format;
        }

        @Override
        public ArtWork call() throws Exception {
            Process pictureProcess = null;

            try {
                String poster = Utils.getTmp(mediaInfo.hashCode(), mediaInfo.hashCode(), "." + format);
                ProcessBuilder pictureProcessBuilder = new ProcessBuilder(FFMPEG,
                        "-i", mediaInfo.getFileName(),
                        poster);
                pictureProcess = pictureProcessBuilder.start();

                StreamCopier.copy(pictureProcess.getInputStream(), System.out);
                // not using redirectErrorStream() as sometimes error stream is not closed by process which cause feature to hang indefinitely
                StreamCopier.copy(pictureProcess.getErrorStream(), System.err);
                pictureProcess.waitFor();
                File posterFile = new File(poster);
                long crc32 = Utils.checksumCRC32(posterFile);
                return new ArtWorkBean(poster, format, crc32);
            } finally {
                Utils.closeSilently(pictureProcess);
            }
        }
    }
}
