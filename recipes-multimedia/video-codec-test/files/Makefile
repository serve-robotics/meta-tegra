# Makefile for Video Codec SDK test program

CC ?= gcc
CFLAGS ?= -O2 -Wall
LDFLAGS ?= -ldl

TARGET = test_video_codec

all: $(TARGET)

$(TARGET): test_video_codec.c
	$(CC) $(CFLAGS) -o $@ $< $(LDFLAGS)

clean:
	rm -f $(TARGET)

install: $(TARGET)
	install -d $(DESTDIR)$(bindir)
	install -m 0755 $(TARGET) $(DESTDIR)$(bindir)/

.PHONY: all clean install
