//
// Created by Cao Minh Vu on 9/4/15.
//

#ifndef INTERACTIVEJOB_IMG_RECOGNITION_H
#define INTERACTIVEJOB_IMG_RECOGNITION_H

#include <opencv2/features2d.hpp>
#include <opencv2/xfeatures2d.hpp>
#include <opencv2/imgcodecs.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/flann/flann.hpp>
#include <opencv2/ml/ml.hpp>

#include <fstream>
#include <dirent.h>
#include <memory>
#include <functional>

extern void predict(std::string classifier, std::string vocabulary_list, cv::Mat img, float threshold, float& result, float& distance);

#endif //INTERACTIVEJOB_IMG_RECOGNITION_H
