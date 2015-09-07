/*
 * main_training.cpp
 *
 *  Created on: Sep 2, 2015
 *      Author: caominhvu
 */




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

using namespace std;

//Prepare resource
std::vector<std::string> object_classes_;
//std::vector<cv::KeyPoint> keypoints_;
//std::vector<std::vector<cv::KeyPoint> > region_keypoints_;
cv::Size size(100,100);

string RESULT_PATH = string("./out");
string DATA_PATH = string("./data");
string VALID_PATH = string("./test");


typedef struct ClassStatistic {
	int number_samples;
	int positive_error;
	int negative_error;
	int correct;
} ClassStatistic;

std::vector<std::string > loadDataset(const char* root)
{
	std::vector<std::string > files;
	DIR* dir = opendir(root);
	struct dirent *ent;
	int count = 0;
	if (dir != NULL) {
		while ((ent = readdir(dir)) != NULL && count <= 2000) {
			if (strcmp(ent->d_name, ".") != 0 && strcmp(ent->d_name, "..") != 0
					&& ent->d_name[0] != '.') {
				count++;
				std::stringstream filename;
				filename << root << "/" << ent->d_name;
				files.push_back(filename.str());
			}
		}
		closedir(dir);
	}

	cout << "Data set" <<endl;
	for(int i=0; i<files.size(); i++) {
		cout << files.at(i).c_str() <<endl;
	}

	return files;
}

void remove_bad_img(std::vector<std::string> files) {
	for (int i = 0; i < files.size(); i++) {
		cv::Mat img = cv::imread(files.at(i).c_str(), CV_LOAD_IMAGE_GRAYSCALE);
		if (img.cols == 0 || img.rows == 0) {
			remove(files.at(i).c_str());
		}
	}
}

void clean(const char* path)
{
	DIR* dir = opendir(path);
	struct dirent *ent;
	if (dir != NULL)
	{
		while ((ent = readdir(dir)) != NULL)
		{
			if (strcmp(ent->d_name, ".") != 0 && strcmp(ent->d_name, "..") != 0)
			{
				std::stringstream classifier_path;
				classifier_path << path << "/" << ent->d_name;
				remove(classifier_path.str().c_str());
			}
		}
		closedir(dir);
	}

	std::vector<std::string> files_for_training = loadDataset(DATA_PATH.c_str());
	remove_bad_img(files_for_training);
	std::vector<std::string> files_for_validating = loadDataset(VALID_PATH.c_str());
	remove_bad_img(files_for_validating);

}


bool readVocabulary( const std::string& filename, cv::Mat& vocabulary )
{
    printf("Reading vocabulary...%s\n", filename.c_str());
    cv::FileStorage fs( filename, cv::FileStorage::READ );
    if( fs.isOpened() )
    {
        fs["vocabulary"] >> vocabulary;
        return true;
    }
    return false;
}
bool writeVocabulary( const std::string& filename, const cv::Mat& vocabulary )
{
    printf("Saving vocabulary...\n");
    cv::FileStorage fs( filename, cv::FileStorage::WRITE );
    if( fs.isOpened() )
    {
        fs << "vocabulary" << vocabulary;
        return true;
    }
    return false;
}

cv::Mat generateVocabulary(const char* vocabulary_path, std::vector<std::string> files, cv::Ptr<cv::FeatureDetector> &detector, cv::Ptr<cv::DescriptorExtractor> &extractor)
{
	cv::Mat vocabulary;
	if(!readVocabulary(vocabulary_path, vocabulary)) {
		// cluster count
		int vocabSize = 100;
		cv::TermCriteria terminate_criterion;
//		terminate_criterion.type = CV_TERMCRIT_ITER;
		terminate_criterion.epsilon = FLT_EPSILON;
//		terminate_criterion.maxCount = 100;
		cv::BOWKMeansTrainer bowTrainer(vocabSize, terminate_criterion, 1,
				cv::KMEANS_PP_CENTERS);

			std::vector<std::string>::const_iterator file;
			for (file = files.begin(); file != files.end(); file++) {
				printf("Build vocabulary...%s\n", (*file).c_str());
				cv::Mat img = cv::imread((*file).c_str(),
						CV_LOAD_IMAGE_GRAYSCALE);

				cv::resize(img, img, size);

				cv::Mat descriptors;
				std::vector<cv::KeyPoint> keypoints;
				detector->detect(img, keypoints);
				extractor->compute(img, keypoints, descriptors);
				if (!descriptors.empty())
					bowTrainer.add(descriptors);
			}

		if (bowTrainer.descriptorsCount() > 0) {
			printf("Clustering ...\n");
			vocabulary.push_back(bowTrainer.cluster());
			bowTrainer.clear();
		}

		writeVocabulary(vocabulary_path, vocabulary);
	}
	return vocabulary;
}

void trainSVM(std::vector<std::string> files)
{
	std::stringstream path_result;
	path_result << RESULT_PATH.c_str() << "/classifier.xml";
	std::ifstream ifile(path_result.str().c_str());
	if (ifile) return; //Already trained, so do nothing


	if (files.size() > 0)
	{
		//Build vocabulary for each vehicle type
		cv::Ptr<cv::Feature2D> detector = cv::xfeatures2d::SurfFeatureDetector::create();
		cv::Ptr<cv::DescriptorExtractor> extractor = cv::xfeatures2d::SurfDescriptorExtractor::create(1);
		cv::Ptr<cv::DescriptorMatcher> matcher = cv::DescriptorMatcher::create("FlannBased");
		cv::BOWImgDescriptorExtractor bowExtractor(extractor, matcher);

		//Get (Generate if not existed) vocabulary
		std::stringstream vocabulary_path;
		vocabulary_path << RESULT_PATH.c_str()<<"/vocabulary.xml";
		cv::Mat vocabulary = generateVocabulary(vocabulary_path.str().c_str(), files, detector,
						extractor);
		bowExtractor.setVocabulary(vocabulary);

		// Training KNN
		cv::Ptr<cv::ml::KNearest> knn = cv::ml::KNearest::create();

		cv::Mat samples;
		cv::Mat responses;
		ofstream mapId2Name;
		stringstream ss;
		ss << RESULT_PATH.c_str() << "/" << "map";
		mapId2Name.open(ss.str().c_str());
		mapId2Name << "{";
		for(int i=0; i<files.size(); i++) {
			cv::Mat img = cv::imread( files.at(i).c_str(), CV_LOAD_IMAGE_GRAYSCALE );
			cv::resize(img, img, size);

			cv::Mat descriptors;
			std::vector<cv::KeyPoint> keypoints;
			detector->detect(img, keypoints);
			bowExtractor.compute(img, keypoints, descriptors);

			if(descriptors.rows == 0 || descriptors.cols==0) {
				cout << "Can't get descriptor:" << files.at(i).c_str();
				remove(files.at(i).c_str());
				continue;
			}
			samples.push_back(descriptors);
			responses.push_back(i);

			if(i !=0) {
				mapId2Name << ",";
			}
			mapId2Name << "\"" << i*1.0f << "\"" << ":" << "\"" << files.at(i).c_str() << "\"";
		}
		mapId2Name << "}";

		if (!samples.empty() && !responses.empty())
		{
			printf("Training with cross validation....%d %d\n", samples.rows, responses.rows);

			knn->train(cv::ml::TrainData::create(samples, cv::ml::ROW_SAMPLE, responses));

			printf("Generate classifier...%s\n", path_result.str().c_str());
			knn->save(path_result.str().c_str());
		}
	}
}

void validation(std::vector<std::string> files) {

	cv::Ptr<cv::FeatureDetector> detector = cv::xfeatures2d::SurfFeatureDetector::create();
	cv::Ptr<cv::DescriptorMatcher> matcher = cv::DescriptorMatcher::create("FlannBased");
	cv::Ptr<cv::DescriptorExtractor> extractor = cv::xfeatures2d::SurfDescriptorExtractor::create();
	cv::Ptr<cv::BOWImgDescriptorExtractor> bowExtractor = new cv::BOWImgDescriptorExtractor(extractor, matcher);

	cv::Mat vocabulary;
	std::stringstream vocabulary_path;
	vocabulary_path << RESULT_PATH.c_str()<<"/vocabulary.xml";
	readVocabulary(vocabulary_path.str(), vocabulary);

	bowExtractor->setVocabulary(vocabulary);

	std::vector<cv::KeyPoint> keypoints;
	cv::Mat descriptors;
//	cv::Ptr<cv::ml::KNearest> knn = cv::ml::KNearest::create();;
	std::stringstream classifier_path;
	classifier_path << RESULT_PATH.c_str() << "/classifier.xml";
	cv::Ptr<cv::ml::KNearest> knn = cv::Algorithm::load<cv::ml::KNearest>(classifier_path.str().c_str());

	for(int i=0; i<files.size(); i++) {
		cv::Mat img = cv::imread(files.at(i).c_str(), CV_LOAD_IMAGE_GRAYSCALE);
		cv::resize(img, img, size);
		cv::Mat descriptors;
		std::vector<cv::KeyPoint> keypoints;
		detector->detect(img, keypoints);
		bowExtractor->compute(img, keypoints, descriptors);

		cv::Mat result, neighbor, dist;
		knn->findNearest(descriptors, 1, result, neighbor, dist);

		cout << files.at(i).c_str() << result << neighbor << dist << endl;

	}
}

void findMinMax(std::vector<std::string> files) {
	int min = 32767;
	int max = -1;
	for(int i=0; i<files.size(); i++) {
		cv::Mat img = cv::imread(files.at(i).c_str(), CV_LOAD_IMAGE_GRAYSCALE);
		if(min > img.cols*img.rows) min = img.cols*img.rows;
		if(max < img.cols*img.rows) max = img.cols*img.rows;
	}
	printf("Max: %d Min: %d", max, min);

}


void sample() {
	cv::Mat src = cv::imread("../data/vietnamworks.png", CV_LOAD_IMAGE_GRAYSCALE);
	cv::Mat src_gray;
	cv::cvtColor( src, src_gray, CV_BGR2GRAY );
	cv::blur( src_gray, src_gray, cv::Size(3,3) );

	cv::Mat canny_output;
	vector<vector<cv::Point> > contours;
	vector<cv::Vec4i> hierarchy;

	/// Detect edges using canny
	int thresh = 3;
	cv::Canny(src_gray, canny_output, thresh, thresh * 2, 3);
	/// Find contours
	cv::findContours(canny_output, contours, hierarchy, CV_RETR_TREE, CV_CHAIN_APPROX_SIMPLE, cv::Point(0, 0) );

	for(int i=0; i<contours.size(); i++) {
		cv::Rect rect = cv::boundingRect(contours.at(i));
		cv::rectangle(src, rect, cv::Scalar(255, 0, 0), 4, 8, 0);
	}
	cv::imshow("Kaka", src);
	cv::waitKey(-1);
}

int main( int _argc, char** _argv )
{

	sample();
//	clean(RESULT_PATH.c_str());
////
//	std::vector<std::string> files_for_training = loadDataset(DATA_PATH.c_str());
//	trainSVM(files_for_training);
//
//	std::vector<std::string> files_for_validating = loadDataset(VALID_PATH.c_str());
//	validation(files_for_validating);

//	std::vector<std::string> files_for_training = loadDataset(DATA_PATH.c_str());
//	findMinMax(files_for_training);
}
