CXXFLAGS =	-O2 -g -Wall -fmessage-length=0

OBJS =		main_training.o

CXXFLAGS = `pkg-config --cflags opencv`

LIBS = `pkg-config --libs opencv`

#LIBS = -L/usr/local/lib -lopencv_features2d -lopencv_xfeatures2d -lopencv_highgui -lopencv_imgproc -lopencv_imgcodecs -lopencv_ml

TARGET =	Train

$(TARGET):	$(OBJS)
	$(CXX) -o $(TARGET) $(OBJS) $(LIBS) $(CXXFLAGS)

all:	$(TARGET)

clean:
	rm -f $(OBJS) $(TARGET)
