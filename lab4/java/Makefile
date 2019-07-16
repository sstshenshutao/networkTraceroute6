TARGET=asciiclient
TARGETJAR=$(TARGET).jar
CLASSFOLDER = classes
SOURCEFOLDER = src
CLASSES=Arguments Assignment4
JAVAFILES = $(addprefix src/, $(addsuffix .java, $(CLASSES)))


#THIS LINE
CLASSES+=

.PHONY: all clean fresh

all: $(TARGET) $(TARGETJAR)

$(TARGETJAR): $(CLASSFOLDER)/.empty $(JAVAFILES)
	javac -encoding UTF-8 -classpath $(SOURCEFOLDER) -d $(CLASSFOLDER) $(JAVAFILES)
	@jar cfm $(TARGETJAR) deps/manifest.txt -C $(CLASSFOLDER) .


#sed replaces CRLF with just LF for windows + java users
$(TARGET): deps/run
	cp deps/run $(TARGET)
	@sed 's/\r$$//' -i $(TARGET)
	@chmod +x $(TARGET)

.PRECIOUS: $(CLASSFOLDER)/.empty
$(CLASSFOLDER)/.empty:
	@mkdir -p $(CLASSFOLDER)
	@touch $(CLASSFOLDER)/.empty

clean:
	-rm -rf $(CLASSFOLDER)
	-rm -f $(TARGET)
	-rm -f $(TARGETJAR)

fresh: clean
	$(MAKE) all
