package com.gzoltar.core.instr;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Label;

import java.io.IOException;
import java.io.InputStream;

public class OffsetPreservingClassReader extends ClassReader {
	public OffsetPreservingClassReader(byte[] b) {
		super(b);
	}

	public OffsetPreservingClassReader(InputStream sourceStream) throws IOException {
		super(sourceStream);
	}

	@Override
	protected Label readLabel(int offset, Label[] labels) {
		if (labels[offset] == null) {
			for (int i = 0; i < labels.length; i++)
				labels[i] = new OffsetPreservingLabel(i);
		}
		return labels[offset];
	}
}
