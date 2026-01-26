###############################################################################
#
# Copyright (c) 2018-2025, NVIDIA CORPORATION.  All rights reserved.
#
# NVIDIA Corporation and its licensors retain all intellectual property
# and proprietary rights in and to this software, related documentation
# and any modifications thereto.  Any use, reproduction, disclosure or
# distribution of this software and related documentation without an express
# license agreement from NVIDIA Corporation is strictly prohibited.
#
###############################################################################

prefix ?= /usr
exec_prefix ?= $(prefix)
libdir ?= $(exec_prefix)/lib

SO_NAME := libgstnvvideo4linux2.so

# Note: libgstnvdsseimeta removed - not available in R38.2.1 without DeepStream
LIBS:= -lnvbufsurface -lnvbufsurftransform -lgstnvcustomhelper

SRCS := $(wildcard *.c)

INCLUDES += -I./ -I../

PKGS := gstreamer-1.0 \
	gstreamer-base-1.0 \
	gstreamer-video-1.0 \
	gstreamer-allocators-1.0 \
	glib-2.0

ifeq ($(USE_LIBV4L2),1)
PKGS += libv4l2
endif

OBJS := $(SRCS:.c=.o)

CFLAGS += -fPIC \
	-DEXPLICITLY_ADDED=1 \
	-DGETTEXT_PACKAGE=1 \
	-DUSE_V4L2_TARGET_NV=1

ifeq ($(USE_LIBV4L2),1)
CFLAGS += -DHAVE_LIBV4L2=1
endif

CFLAGS += $(shell pkg-config --cflags $(PKGS))

LDFLAGS += -Wl,--no-undefined

LIBS += $(shell pkg-config --libs $(PKGS))

.PHONY: all
all: $(SO_NAME)

%.o: %.c
	$(CC) -c $< $(CFLAGS) $(INCLUDES) -o $@

$(SO_NAME): $(OBJS)
	$(CC) -shared -o $(SO_NAME) $(OBJS) $(LIBS) $(LDFLAGS)

.PHONY: install
install: $(SO_NAME)
	install -d $(DESTDIR)$(libdir)/gstreamer-1.0
	install -m 0644 $(SO_NAME) $(DESTDIR)$(libdir)/gstreamer-1.0

.PHONY: clean
clean:
	rm -rf $(OBJS) $(SO_NAME)
