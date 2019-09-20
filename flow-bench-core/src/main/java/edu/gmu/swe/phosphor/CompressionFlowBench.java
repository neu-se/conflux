package edu.gmu.swe.phosphor;

import edu.gmu.swe.phosphor.ignored.runtime.MultiLabelFlowBenchResult;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;

import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static edu.gmu.swe.phosphor.FlowBenchUtil.taintWithIndices;

/**
 * Tests implicit flows found in Apache Common Compress
 */
public class CompressionFlowBench {

    /**
     * Compresses the specified input bytes using bzip2. Returns the compressed bytes.
     */
    private byte[] bzip2Compress(byte[] input) throws IOException {
        try(InputStream in = new ByteArrayInputStream(input)) {
            try(ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                try(BZip2CompressorOutputStream bzOut = new BZip2CompressorOutputStream(out)) {
                    final byte[] buffer = new byte[1024];
                    int n;
                    while(-1 != (n = in.read(buffer))) {
                        bzOut.write(buffer, 0, n);
                    }
                    bzOut.close();
                    in.close();
                    return out.toByteArray();
                }
            }
        }
    }

    /**
     * Decompresses the specified input bytes using bzip2. Returns the decompressed bytes.
     */
    private byte[] bzip2Decompress(byte[] input) throws IOException {
        try(InputStream in = new ByteArrayInputStream(input)) {
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                try (BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(in)) {
                    final byte[] buffer2 = new byte[1024];
                    int n;
                    while (-1 != (n = bzIn.read(buffer2))) {
                        out.write(buffer2, 0, n);
                    }
                    return out.toByteArray();
                }
            }
        }
    }

    /**
     * Compresses then decompresses a byte array using Apache Commons' bzip2 compressor and decompressor. Checks that
     * the output is labeled the same as the input.
     */
    @FlowBench(requiresBitLevelPrecision = true)
    public void testBzip2RoundTrip(MultiLabelFlowBenchResult benchResult, TaintedPortionPolicy policy) throws IOException {
        String value = "Porttttttttitor leo a diam       tempor";
        byte[] input = taintWithIndices((value + value).getBytes(), policy);
        byte[] output = bzip2Decompress(bzip2Compress(input));
        for(int i = 0; i < input.length; i++) {
            if(policy.inTaintedRange(i, input.length)) {
                benchResult.check(Collections.singletonList(i), output[i]);
            } else {
                benchResult.check(Collections.emptyList(), output[i]);
            }
        }
    }

    /**
     * Compresses a byte array using Apache Commons' bzip2 compressor. Checks that all of the labels present on the
     * input are present on the output.
     */
    @FlowBench
    public void testBzip2Compress(MultiLabelFlowBenchResult benchResult) throws IOException {
        byte[] input = taintWithIndices("Porttttttttitor leo a diam       tempor".getBytes());
        byte[] output = bzip2Compress(input);
        List<Integer> expected = IntStream.range(0, input.length).boxed().collect(Collectors.toList());
        benchResult.check(expected, output);
    }
}
