//
// Created by Cao Minh Vu on 9/4/15.
//

#include "img_recognition.h"
#include "wrapper_log.h"

using namespace std;

cv::Size size(100,100);

string RESULT_PATH = string("./out");
string DATA_PATH = string("./data");
string VALID_PATH = string("./test");

cv::Mat parseVocabulary(const std::string data) {
    cv::Mat vocabulary;
    cv::FileStorage fs(data, cv::FileStorage::READ | cv::FileStorage::MEMORY);
    fs["vocabulary"] >> vocabulary;
    LOGI("Reading vocabulary...%d %d", vocabulary.rows, vocabulary.cols);
    fs.release();
    return vocabulary;
}

void predict(std::string classifier, std::string vocabulary_list, cv::Mat img, float threshold, float& resultdata, float& disancedata) {

    LOGI("Predicting...");
    resultdata = -1;
    disancedata = -1;
    if (img.cols == 0 || img.rows == 0) return;
    cv::Ptr<cv::FeatureDetector> detector = cv::xfeatures2d::SurfFeatureDetector::create();
    cv::Ptr<cv::DescriptorMatcher> matcher = cv::DescriptorMatcher::create("FlannBased");
    cv::Ptr<cv::DescriptorExtractor> extractor = cv::xfeatures2d::SurfDescriptorExtractor::create();
    cv::Ptr<cv::BOWImgDescriptorExtractor> bowExtractor = new cv::BOWImgDescriptorExtractor(extractor, matcher);

    cv::Mat vocabulary = parseVocabulary(vocabulary_list);
    bowExtractor->setVocabulary(vocabulary);

    cv::Ptr<cv::ml::KNearest> knn = cv::Algorithm::loadFromString<cv::ml::KNearest>(classifier);

    cv::resize(img, img, size);
    cv::Mat descriptors;
    std::vector<cv::KeyPoint> keypoints;
    detector->detect(img, keypoints);

    if (keypoints.size() <= 0) return;
    bowExtractor->compute(img, keypoints, descriptors);
    if (descriptors.rows == 0 || descriptors.cols == 0) return;

    cv::Mat result, neighbor, dist;

    knn->findNearest(descriptors, 1, result, neighbor, dist);

    LOGI("Result: %f %f \n", result.at<float>(0, 0), dist.at<float>(0, 0));

    resultdata = result.at<float>(0, 0);
    disancedata = dist.at<float>(0, 0);

}