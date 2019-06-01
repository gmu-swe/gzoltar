/**
 * Copyright (C) 2019 GZoltar contributors.
 * 
 * This file is part of GZoltar.
 * 
 * GZoltar is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * 
 * GZoltar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with GZoltar. If
 * not, see <https://www.gnu.org/licenses/>.
 */
package com.gzoltar.internal.core.instr;

import com.gzoltar.internal.core.AgentConfigs;
import com.gzoltar.internal.core.runtime.Collector;
import com.gzoltar.internal.core.runtime.ProbeGroup;
import com.gzoltar.internal.core.util.MD5;
import org.jacoco.core.internal.ContentTypeDetector;
import org.jacoco.core.internal.Pack200Streams;
import org.jacoco.core.internal.instr.SignatureRemover;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Several APIs to instrument Java class definitions for coverage tracing.
 */
public class Instrumenter {

  private final SignatureRemover signatureRemover;
  private final AgentConfigs agentConfigs;

  /**
   * 
   * @param agentConfigs
   */
  public Instrumenter(final AgentConfigs agentConfigs) {
    this.signatureRemover = new SignatureRemover();
    this.agentConfigs = agentConfigs;
  }

  /**
   * Determines whether signatures should be removed from JAR files. This is typically necessary as
   * instrumentation modifies the class files and therefore invalidates existing JAR signatures.
   * Default is <code>true</code>.
   * 
   * @param flag <code>true</code> if signatures should be removed
   */
  public void setRemoveSignatures(final boolean flag) {
    this.signatureRemover.setActive(flag);
  }

  /**
   * 
   * @param classfileBuffer
   * @return
   * @throws Exception
   */
  public byte[] instrument(final byte[] classfileBuffer) throws Exception {
    return this.instrument(new ByteArrayInputStream(classfileBuffer));
  }

  /**
   * 
   * @param sourceStream
   * @return
   * @throws Exception
   */
  public byte[] instrument(final InputStream sourceStream) throws Exception {
    ClassReader cr = new ClassReader(sourceStream);
    //Check for existing instrumentation
    ClassNode cn = new ClassNode();
    cr.accept(cn, ClassReader.SKIP_CODE);
    ClassWriter cw = new ClassWriter(cr, 0);
    cr.accept(cw, 0);
    byte[] originalBytes = cw.toByteArray();

    //Skip all interfaces
    if((cn.access & Opcodes.ACC_INTERFACE) != 0)
      return originalBytes;

    cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
    for(FieldNode each : cn.fields)
    {
      if(each.name.equals(InstrumentationConstants.FIELD_NAME)) {
        cr.accept(cw,0);
        return cw.toByteArray();
      }
    }

    String hash = MD5.calculateHash(originalBytes);
    ProbeGroup probeGroup = new ProbeGroup(hash, cn.name);

//    CheckClassAdapter cca = new CheckClassAdapter(cw, false);
    CoverageClassVisitor cv = new CoverageClassVisitor(cw, agentConfigs, probeGroup);
    cr.accept(cv, ClassReader.EXPAND_FRAMES);
    byte[] ret = cw.toByteArray();

    Collector.instance().regiterProbeGroup(probeGroup);



//    FileOutputStream fos = new FileOutputStream("debug/"+cn.name.replace('/','.')+".class");
//    fos.write(ret);
//    fos.close();
    return ret;
  }

  /**
   * 
   * @param cc
   * @return
   * @throws Exception
   */
  public byte[] instrument(final ClassNode cc) throws Exception {
  	throw new UnsupportedOperationException();
  }

  public static void main(String[] args) throws Throwable{
    Instrumenter i = new Instrumenter(new AgentConfigs());
    i.instrument(new File("z.class").getAbsoluteFile(), new File("inst/z.class").getAbsoluteFile());
  }

  /**
   * Creates a instrumented version of the given resource depending on its type. Class files and the
   * content of archive files (.zip, .jar) are instrumented. All other files are copied without
   * modification.
   * 
   * @param input stream to contents from
   * @param output stream to write the instrumented version of the contents
   * @return number of instrumented classes
   * @throws Exception if reading data from the stream fails or a class cannot be instrumented
   */
  public int instrumentToFile(final InputStream input, final OutputStream output) throws Exception {
    final ContentTypeDetector detector = new ContentTypeDetector(input);
    switch (detector.getType()) {
      case ContentTypeDetector.CLASSFILE:
        output.write(this.instrument(detector.getInputStream()));
        return 1;
      case ContentTypeDetector.GZFILE:
        return this.instrumentGzip(detector.getInputStream(), output);
      case ContentTypeDetector.PACK200FILE:
        return this.instrumentPack200(detector.getInputStream(), output);
      case ContentTypeDetector.ZIPFILE:
        return this.instrumentZip(detector.getInputStream(), output);
      case ContentTypeDetector.UNKNOWN:
      default:
        this.copy(detector.getInputStream(), output);
        return 0;
    }
  }

  public int instrument(File source, File dest) throws Exception {
    final InputStream input = new FileInputStream(source);
    try {
      OutputStream output = null;
      if(dest != null) {
        dest.getParentFile().mkdirs();
        output = new FileOutputStream(dest);
      }
      else {
        output = new OutputStream() {
          @Override
          public void write(int b) throws IOException {

          }
        };
      }
      try {
        return this.instrumentToFile(input, output);
      } finally {
        output.close();
      }
    } catch (Exception e) {
      throw e;
    } finally {
      input.close();
    }
  }

  public int instrumentRecursively(File source, File dest)
      throws Exception {
    int numInstrumentedClasses = 0;

    if (source.isDirectory()) {
      for (final File child : source.listFiles()) {
      	if(dest == null)
          numInstrumentedClasses += this.instrumentRecursively(child, null);
      	else
          numInstrumentedClasses += this.instrumentRecursively(child, new File(dest, child.getName()));
      }
    } else {
      numInstrumentedClasses += this.instrument(source, dest);
    }

    return numInstrumentedClasses;
  }

  private int instrumentGzip(final InputStream input, final OutputStream output) throws Exception {
    final GZIPInputStream gzipInputStream = new GZIPInputStream(input);
    final GZIPOutputStream gzipOutputStream = new GZIPOutputStream(output);
    final int count = this.instrumentToFile(gzipInputStream, gzipOutputStream);
    gzipOutputStream.finish();
    return count;
  }

  private int instrumentPack200(final InputStream input, final OutputStream output)
      throws Exception {
    final InputStream unpackedInput = Pack200Streams.unpack(input);
    final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    final int count = this.instrumentToFile(unpackedInput, buffer);
    Pack200Streams.pack(buffer.toByteArray(), output);
    return count;
  }

  private int instrumentZip(final InputStream input, final OutputStream output) throws Exception {
    final ZipInputStream zipInputStream = new ZipInputStream(input);
    final ZipOutputStream zipOutputStream = new ZipOutputStream(output);
    ZipEntry entry;
    int count = 0;

    while ((entry = zipInputStream.getNextEntry()) != null) {
      final String entryName = entry.getName();
      if (this.signatureRemover.removeEntry(entryName)) {
        continue;
      }

      zipOutputStream.putNextEntry(new ZipEntry(entryName));
      if (!this.signatureRemover.filterEntry(entryName, zipInputStream, zipOutputStream)) {
        count += this.instrumentToFile(zipInputStream, zipOutputStream);
      }
      zipOutputStream.closeEntry();
    }
    zipOutputStream.finish();

    return count;
  }

  private void copy(final InputStream input, final OutputStream output) throws IOException {
    final byte[] buffer = new byte[1024];
    int len;
    while ((len = input.read(buffer)) != -1) {
      output.write(buffer, 0, len);
    }
  }

  public void analyzeRecursively(String buildLocation) throws Exception {
  	File sourceFile = new File(buildLocation);
  	instrumentRecursively(sourceFile, null);
  }

  public void instrument(String classUnderTest) {
    try {
    	InputStream is = ClassLoader.getSystemResourceAsStream(classUnderTest.replace('.', '/')
                + ".class");
    	instrument(is);
    	is.close();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
