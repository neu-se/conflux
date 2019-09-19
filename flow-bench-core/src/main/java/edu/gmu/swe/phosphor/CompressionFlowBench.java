package edu.gmu.swe.phosphor;

import edu.gmu.swe.phosphor.ignored.maven.MultiLabelFlowBenchResult;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;

import java.io.*;

public class CompressionFlowBench extends BaseFlowBench {

    @FlowBench
    public void testBzip2Compress(MultiLabelFlowBenchResult benchResult) throws IOException {
        byte[] input = taintWithIndices("Porttttttttitor leo a diam       tempor".getBytes());
        InputStream in = new ByteArrayInputStream(input);
        OutputStream out = new ByteArrayOutputStream();
        BZip2CompressorOutputStream bzOut = new BZip2CompressorOutputStream(out);
        final byte[] buffer = new byte[1024];
        int n;
        while (-1 != (n = in.read(buffer))) {
            bzOut.write(buffer, 0, n);
        }
        bzOut.close();
        in.close();
    }
}
