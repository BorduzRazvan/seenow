package work.seenow.seenow.Utils;


import android.util.Log;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_objdetect;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;


import work.seenow.seenow.R;

import static org.bytedeco.javacpp.helper.opencv_objdetect.cvHaarDetectObjects;
import static org.bytedeco.javacpp.opencv_core.cvGetSeqElem;
import static org.bytedeco.javacpp.opencv_core.cvLoad;
import static org.bytedeco.javacpp.opencv_core.cvReleaseMemStorage;
import static org.bytedeco.javacpp.opencv_core.cvSize;
import static org.bytedeco.javacpp.opencv_objdetect.CV_HAAR_DO_CANNY_PRUNING;

public class HaarFaceDetector {
    private static final String TAG = HaarFaceDetector.class.getSimpleName();
    private opencv_objdetect.CvHaarClassifierCascade haarClassifierCascade;
    private opencv_core.CvMemStorage storage;

    public HaarFaceDetector() {

        try {
            File haarCascade = new File("haarcascade_frontalface_default.xml");
            haarClassifierCascade = new opencv_objdetect.CvHaarClassifierCascade(cvLoad(haarCascade.getAbsolutePath()));
            haarClassifierCascade.load(haarCascade.getAbsolutePath(),cvSize(0, 0));
        } catch (Exception e) {
            Log.d(TAG,"Error when trying to get the haar cascade"+e);
            throw new IllegalStateException("Error when trying to get the haar cascade", e);
        }
        storage = opencv_core.CvMemStorage.create();
    }

    /**
     * Detects and returns a map of cropped faces from a given captured frame
     *
     * @param frame the frame captured by the {@link org.bytedeco.javacv.FrameGrabber}
     * @return A map of faces along with their coordinates in the frame
     */
    public int detectFace(opencv_core.IplImage Image) {
        Map<opencv_core.CvRect, opencv_core.Mat> detectedFaces = new HashMap<>();

        opencv_core.IplImage iplImage = Image;

        /*
         * return a CV Sequence (kind of a list) with coordinates of rectangle face area.
         * (returns coordinates of left top corner & right bottom corner)
         */
        opencv_core.CvSeq detectObjects = cvHaarDetectObjects(iplImage, haarClassifierCascade, storage, 1.5, 3, CV_HAAR_DO_CANNY_PRUNING);

        int numberOfPeople = detectObjects.total();

        return numberOfPeople;
    }

    @Override
    public void finalize() {
        cvReleaseMemStorage(storage);
    }
}