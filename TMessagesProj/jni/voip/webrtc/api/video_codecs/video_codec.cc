/*
 *  Copyright (c) 2012 The WebRTC project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

#include "api/video_codecs/video_codec.h"

#include <string.h>

#include <string>

#include "absl/strings/match.h"
#include "rtc_base/checks.h"

namespace webrtc {
namespace {
constexpr char kPayloadNameVp8[] = "VP8";
constexpr char kPayloadNameVp9[] = "VP9";
// TODO(bugs.webrtc.org/11042): Rename to AV1 when rtp payload format for av1 is
// frozen.
constexpr char kPayloadNameAv1[] = "AV1X";
constexpr char kPayloadNameH264[] = "H264";
#ifndef DISABLE_H265
constexpr char kPayloadNameH265[] = "H265";
#endif
constexpr char kPayloadNameGeneric[] = "Generic";
constexpr char kPayloadNameMultiplex[] = "Multiplex";
}  // namespace

bool VideoCodecVP8::operator==(const VideoCodecVP8& other) const {
  return (complexity == other.complexity &&
          numberOfTemporalLayers == other.numberOfTemporalLayers &&
          denoisingOn == other.denoisingOn &&
          automaticResizeOn == other.automaticResizeOn &&
          frameDroppingOn == other.frameDroppingOn &&
          keyFrameInterval == other.keyFrameInterval);
}

bool VideoCodecVP9::operator==(const VideoCodecVP9& other) const {
  return (complexity == other.complexity &&
          numberOfTemporalLayers == other.numberOfTemporalLayers &&
          denoisingOn == other.denoisingOn &&
          frameDroppingOn == other.frameDroppingOn &&
          keyFrameInterval == other.keyFrameInterval &&
          adaptiveQpMode == other.adaptiveQpMode &&
          automaticResizeOn == other.automaticResizeOn &&
          numberOfSpatialLayers == other.numberOfSpatialLayers &&
          flexibleMode == other.flexibleMode);
}

bool VideoCodecH264::operator==(const VideoCodecH264& other) const {
  return (frameDroppingOn == other.frameDroppingOn &&
          keyFrameInterval == other.keyFrameInterval &&
          numberOfTemporalLayers == other.numberOfTemporalLayers);
}

#ifndef DISABLE_H265
bool VideoCodecH265::operator==(const VideoCodecH265& other) const {
  return (frameDroppingOn == other.frameDroppingOn &&
          keyFrameInterval == other.keyFrameInterval &&
          vpsLen == other.vpsLen && spsLen == other.spsLen &&
          ppsLen == other.ppsLen &&
          (spsLen == 0 || memcmp(spsData, other.spsData, spsLen) == 0) &&
          (ppsLen == 0 || memcmp(ppsData, other.ppsData, ppsLen) == 0));
}
#endif

bool SpatialLayer::operator==(const SpatialLayer& other) const {
  return (width == other.width && height == other.height &&
          maxFramerate == other.maxFramerate &&
          numberOfTemporalLayers == other.numberOfTemporalLayers &&
          maxBitrate == other.maxBitrate &&
          targetBitrate == other.targetBitrate &&
          minBitrate == other.minBitrate && qpMax == other.qpMax &&
          active == other.active);
}

VideoCodec::VideoCodec()
    : codecType(kVideoCodecGeneric),
      plType(0),
      width(0),
      height(0),
      startBitrate(0),
      maxBitrate(0),
      minBitrate(0),
      maxFramerate(0),
      active(true),
      qpMax(0),
      numberOfSimulcastStreams(0),
      simulcastStream(),
      spatialLayers(),
      mode(VideoCodecMode::kRealtimeVideo),
      expect_encode_from_texture(false),
      timing_frame_thresholds({0, 0}),
      legacy_conference_mode(false),
      codec_specific_() {}

VideoCodecVP8* VideoCodec::VP8() {
  RTC_DCHECK_EQ(codecType, kVideoCodecVP8);
  return &codec_specific_.VP8;
}

const VideoCodecVP8& VideoCodec::VP8() const {
  RTC_DCHECK_EQ(codecType, kVideoCodecVP8);
  return codec_specific_.VP8;
}

VideoCodecVP9* VideoCodec::VP9() {
  RTC_DCHECK_EQ(codecType, kVideoCodecVP9);
  return &codec_specific_.VP9;
}

const VideoCodecVP9& VideoCodec::VP9() const {
  RTC_DCHECK_EQ(codecType, kVideoCodecVP9);
  return codec_specific_.VP9;
}

VideoCodecH264* VideoCodec::H264() {
  RTC_DCHECK_EQ(codecType, kVideoCodecH264);
  return &codec_specific_.H264;
}

const VideoCodecH264& VideoCodec::H264() const {
  RTC_DCHECK_EQ(codecType, kVideoCodecH264);
  return codec_specific_.H264;
}

#ifndef DISABLE_H265
VideoCodecH265* VideoCodec::H265() {
  RTC_DCHECK_EQ(codecType, kVideoCodecH265);
  return &codec_specific_.H265;
}

const VideoCodecH265& VideoCodec::H265() const {
  RTC_DCHECK_EQ(codecType, kVideoCodecH265);
  return codec_specific_.H265;
}
#endif

const char* CodecTypeToPayloadString(VideoCodecType type) {
  switch (type) {
    case kVideoCodecVP8:
      return kPayloadNameVp8;
    case kVideoCodecVP9:
      return kPayloadNameVp9;
    case kVideoCodecAV1:
      return kPayloadNameAv1;
    case kVideoCodecH264:
      return kPayloadNameH264;
#ifndef DISABLE_H265
    case kVideoCodecH265:
      return kPayloadNameH265;
#endif
    case kVideoCodecMultiplex:
      return kPayloadNameMultiplex;
    case kVideoCodecGeneric:
	default:
      return kPayloadNameGeneric;
  }
}

VideoCodecType PayloadStringToCodecType(const std::string& name) {
  if (absl::EqualsIgnoreCase(name, kPayloadNameVp8))
    return kVideoCodecVP8;
  if (absl::EqualsIgnoreCase(name, kPayloadNameVp9))
    return kVideoCodecVP9;
  if (absl::EqualsIgnoreCase(name, kPayloadNameAv1))
    return kVideoCodecAV1;
  if (absl::EqualsIgnoreCase(name, kPayloadNameH264))
    return kVideoCodecH264;
  if (absl::EqualsIgnoreCase(name, kPayloadNameMultiplex))
    return kVideoCodecMultiplex;
#ifndef DISABLE_H265
  if (absl::EqualsIgnoreCase(name, kPayloadNameH265))
    return kVideoCodecH265;
#endif
  return kVideoCodecGeneric;
}

}  // namespace webrtc