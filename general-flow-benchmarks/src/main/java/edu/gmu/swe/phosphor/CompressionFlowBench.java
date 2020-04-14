package edu.gmu.swe.phosphor;

import edu.gmu.swe.phosphor.ignored.runtime.FlowBenchResultImpl;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.compress.compressors.lz4.BlockLZ4CompressorInputStream;
import org.apache.commons.compress.compressors.lz4.BlockLZ4CompressorOutputStream;
import org.apache.commons.compress.compressors.lzma.LZMACompressorInputStream;
import org.apache.commons.compress.compressors.lzma.LZMACompressorOutputStream;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static edu.gmu.swe.phosphor.FlowBenchUtil.taintWithIndices;

/**
 * Tests implicit flows found in compression and decompression methods.
 */
public class CompressionFlowBench {

    /**
     * Compresses the specified input bytes using the specified compressor class. Returns the compressed bytes.
     */
    private byte[] compressBytes(byte[] input, Class<? extends CompressorOutputStream> compressorType)
            throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        try(InputStream in = new ByteArrayInputStream(input)) {
            try(ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                Constructor<? extends CompressorOutputStream> compressorCons = compressorType.getConstructor(OutputStream.class);
                try(CompressorOutputStream compressorOut = compressorCons.newInstance(out)) {
                    final byte[] buffer = new byte[1024];
                    int n;
                    while(-1 != (n = in.read(buffer))) {
                        compressorOut.write(buffer, 0, n);
                    }
                    compressorOut.close();
                    in.close();
                    return out.toByteArray();
                }
            }
        }
    }

    /**
     * Decompresses the specified input bytes using the specified decompressor class. Returns the decompressed bytes.
     */
    private byte[] decompressBytes(byte[] input, Class<? extends CompressorInputStream> decompressorType)
            throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        try(InputStream in = new ByteArrayInputStream(input)) {
            try(ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                Constructor<? extends CompressorInputStream> compressorCons = decompressorType.getConstructor(InputStream.class);
                try(CompressorInputStream compressorIn = compressorCons.newInstance(in)) {
                    final byte[] buffer2 = new byte[1024];
                    int n;
                    while(-1 != (n = compressorIn.read(buffer2))) {
                        out.write(buffer2, 0, n);
                    }
                    out.close();
                    compressorIn.close();
                    return out.toByteArray();
                }
            }
        }
    }

    /**
     * Compresses then decompresses a byte array using the specified compressor and decompressor types. Checks that
     * the output is labeled the same as the input.
     */
    private void checkCompressionRoundTrip(FlowBenchResultImpl benchResult, TaintedPortionPolicy policy, Class<? extends CompressorOutputStream> compressorType,
                                           Class<? extends CompressorInputStream> decompressorType) throws Exception {
        String value = "Porttttttttitor leo a diam       tempor";
        byte[] input = taintWithIndices((value + value).getBytes(), policy);
        byte[] output = decompressBytes(compressBytes(input, compressorType), decompressorType);
        for(int i = 0; i < input.length; i++) {
            if(policy.inTaintedRange(i, input.length)) {
                benchResult.check(Collections.singletonList(i), output[i]);
            } else {
                benchResult.checkEmpty(output[i]);
            }
        }
    }

    /**
     * Compresses a byte array using the specified compressor type. Checks that all of the labels present on the
     * input are present on the output.
     */
    private void checkCompression(FlowBenchResultImpl benchResult, Class<? extends CompressorOutputStream> compressorType) throws Exception {
        byte[] input = taintWithIndices("Porttttttttitor leo a diam       tempor".getBytes());
        byte[] output = compressBytes(input, compressorType);
        List<Integer> expected = IntStream.range(0, input.length).boxed().collect(Collectors.toList());
        benchResult.check(expected, output);
    }

    /**
     * Checks round-trip compression using Apache Commons' bzip2 compressor and decompressor.
     */
    @FlowBench(requiresBitLevelPrecision = true, group = "compress-round-trip", project = "commons-compress", implementation = "BZip2Compressor")
    public void BZip2RoundTrip(FlowBenchResultImpl benchResult, TaintedPortionPolicy policy) throws Exception {
        checkCompressionRoundTrip(benchResult, policy, BZip2CompressorOutputStream.class, BZip2CompressorInputStream.class);
    }

    /**
     * Checks one-way compression using Apache Commons' bzip2 compressor.
     */
    @FlowBench(requiresBitLevelPrecision = true, group = "compress", project = "commons-compress", implementation = "BZip2Compressor")
    public void BZip2Compress(FlowBenchResultImpl benchResult) throws Exception {
        checkCompression(benchResult, BZip2CompressorOutputStream.class);
    }

    /**
     * Checks round-trip compression using Tukaani's LZMA compressor and decompressor.
     */
    @FlowBench(requiresBitLevelPrecision = true, group = "compress-round-trip", project = "tukaani-xz", implementation = "LZMACompressor")
    public void LZMARoundTrip(FlowBenchResultImpl benchResult, TaintedPortionPolicy policy) throws Exception {
        checkCompressionRoundTrip(benchResult, policy, LZMACompressorOutputStream.class, LZMACompressorInputStream.class);
    }

    /**
     * Checks one-way compression using Tukaani's LZMA compressor.
     */
    @FlowBench(group = "compress", project = "tukaani-xz", implementation = "LZMACompressor")
    public void LZMACompress(FlowBenchResultImpl benchResult) throws Exception {
        checkCompression(benchResult, LZMACompressorOutputStream.class);
    }

    /**
     * Checks round-trip compression using Apache Commons' block LZ4 compressor and decompressor.
     */
    @FlowBench(requiresBitLevelPrecision = true, group = "compress-round-trip", project = "tukaani-xz", implementation = "LZMACompressor")
    public void blockLZ4RoundTrip(FlowBenchResultImpl benchResult, TaintedPortionPolicy policy) throws Exception {
        checkCompressionRoundTrip(benchResult, policy, BlockLZ4CompressorOutputStream.class, BlockLZ4CompressorInputStream.class);
    }

    /**
     * Checks one-way compression using Apache Commons' block LZ4 compressor.
     */
    @FlowBench(group = "compress", project = "tukaani-xz", implementation = "BlockLZ4Compressor")
    public void blockLZ4Compress(FlowBenchResultImpl benchResult) throws Exception {
        checkCompression(benchResult, BlockLZ4CompressorOutputStream.class);
    }
}
