# Makefile for Video Codec SDK test programs

CC ?= gcc
CFLAGS ?= -O2 -Wall
LDFLAGS ?= -ldl

TARGETS = test_video_codec test_nvvideo test_nvmedia_iep test_nvmedia_ide

all: $(TARGETS)

test_video_codec: test_video_codec.c
	$(CC) $(CFLAGS) -o $@ $< $(LDFLAGS)

test_nvvideo: test_nvvideo.c
	$(CC) $(CFLAGS) -o $@ $< $(LDFLAGS)

test_nvmedia_iep: test_nvmedia_iep.c
	$(CC) $(CFLAGS) -o $@ $< $(LDFLAGS)

test_nvmedia_ide: test_nvmedia_ide.c
	$(CC) $(CFLAGS) -o $@ $< $(LDFLAGS)

clean:
	rm -f $(TARGETS)

install: $(TARGETS)
	install -d $(DESTDIR)$(bindir)
	install -m 0755 $(TARGETS) $(DESTDIR)$(bindir)/

.PHONY: all clean install
