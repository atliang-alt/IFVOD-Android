package com.cqcsy.lgsp.preload

import android.net.Uri
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.offline.SegmentDownloader
import com.google.android.exoplayer2.source.hls.playlist.HlsMasterPlaylist
import com.google.android.exoplayer2.source.hls.playlist.HlsMediaPlaylist
import com.google.android.exoplayer2.source.hls.playlist.HlsPlaylist
import com.google.android.exoplayer2.source.hls.playlist.HlsPlaylistParser
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.ParsingLoadable
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.util.UriUtil
import java.io.IOException
import java.util.concurrent.Executor

/**
 * 作者：wangjianxiong
 * 创建时间：2023/3/17
 *
 * 对于hls格式来说，只下载第一片;如果需要缓存完整的切片，请使用[com.google.android.exoplayer2.source.hls.offline.HlsDownloader]
 */
class HlsPreloadDownloader : SegmentDownloader<HlsPlaylist> {

    constructor(
        mediaItem: MediaItem,
        cacheDataSourceFactory: CacheDataSource.Factory
    ) : this(mediaItem, cacheDataSourceFactory, Executor { obj: Runnable -> obj.run() })

    constructor(
        mediaItem: MediaItem, cacheDataSourceFactory: CacheDataSource.Factory, executor: Executor
    ) : this(mediaItem, HlsPlaylistParser(), cacheDataSourceFactory, executor)

    constructor(
        mediaItem: MediaItem,
        manifestParser: ParsingLoadable.Parser<HlsPlaylist>,
        cacheDataSourceFactory: CacheDataSource.Factory,
        executor: Executor
    ) : super(mediaItem, manifestParser, cacheDataSourceFactory, executor)

    override fun getSegments(
        dataSource: DataSource,
        playlist: HlsPlaylist,
        removing: Boolean
    ): MutableList<Segment> {
        val mediaPlaylistDataSpecs = mutableListOf<DataSpec>()
        if (playlist is HlsMasterPlaylist) {
            addMediaPlaylistDataSpecs(playlist.mediaPlaylistUrls, mediaPlaylistDataSpecs)
        } else {
            mediaPlaylistDataSpecs.add(
                getCompressibleDataSpec(Uri.parse(playlist.baseUri))
            )
        }

        val segments = mutableListOf<Segment>()
        val seenEncryptionKeyUris = hashSetOf<Uri>()
        for (mediaPlaylistDataSpec in mediaPlaylistDataSpecs) {
            segments.add(
                Segment( /* startTimeUs= */0,
                    mediaPlaylistDataSpec
                )
            )
            val mediaPlaylist = try {
                getManifest(dataSource, mediaPlaylistDataSpec, removing) as HlsMediaPlaylist
            } catch (e: IOException) {
                if (!removing) {
                    throw e
                }
                continue
            }
            val hlsSegments = mediaPlaylist.segments
            if (hlsSegments.isNotEmpty()) {
                //此处只缓存第一片
                addSegment(mediaPlaylist, hlsSegments[0], seenEncryptionKeyUris, segments)
                return segments
            }
            /*var lastInitSegment: HlsMediaPlaylist.Segment? = null
            for (i in hlsSegments.indices) {
                val segment = hlsSegments[i]
                val initSegment = segment.initializationSegment
                if (initSegment != null && initSegment != lastInitSegment) {
                    lastInitSegment = initSegment
                    addSegment(mediaPlaylist, initSegment, seenEncryptionKeyUris, segments)
                }
                addSegment(mediaPlaylist, segment, seenEncryptionKeyUris, segments)
            }*/
        }
        return segments
    }


    private fun addMediaPlaylistDataSpecs(
        mediaPlaylistUrls: List<Uri>,
        out: MutableList<DataSpec>
    ) {
        for (i in mediaPlaylistUrls.indices) {
            out.add(
                getCompressibleDataSpec(
                    mediaPlaylistUrls[i]
                )
            )
        }
    }

    private fun addSegment(
        mediaPlaylist: HlsMediaPlaylist,
        segment: HlsMediaPlaylist.Segment,
        seenEncryptionKeyUris: HashSet<Uri>,
        out: MutableList<Segment>
    ) {
        val baseUri = mediaPlaylist.baseUri
        val startTimeUs = mediaPlaylist.startTimeUs + segment.relativeStartTimeUs
        if (segment.fullSegmentEncryptionKeyUri != null) {
            val keyUri = UriUtil.resolveToUri(baseUri, segment.fullSegmentEncryptionKeyUri)
            if (seenEncryptionKeyUris.add(keyUri)) {
                out.add(Segment(startTimeUs, getCompressibleDataSpec(keyUri)))
            }
        }
        val segmentUri = UriUtil.resolveToUri(baseUri, segment.url)
        val dataSpec = DataSpec(segmentUri, segment.byteRangeOffset, segment.byteRangeLength)
        out.add(Segment(startTimeUs, dataSpec))
    }
}