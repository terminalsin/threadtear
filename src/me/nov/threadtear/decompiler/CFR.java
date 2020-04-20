package me.nov.threadtear.decompiler;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import org.benf.cfr.reader.api.CfrDriver;
import org.benf.cfr.reader.api.OutputSinkFactory;
import org.benf.cfr.reader.api.SinkReturns;
import org.benf.cfr.reader.apiunreleased.ClassFileSource2;
import org.benf.cfr.reader.apiunreleased.JarContent;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.Pair;
import org.benf.cfr.reader.util.AnalysisType;
import org.objectweb.asm.tree.ClassNode;

import me.nov.threadtear.io.Conversion;

public class CFR {

	public static final HashMap<String, String> options = new HashMap<>();

	static {
		options.put("aexagg", "false");
		options.put("allowcorrecting", "true");
		options.put("arrayiter", "true");
		options.put("caseinsensitivefs", "false");
		options.put("clobber", "false");
		options.put("collectioniter", "true");
		options.put("commentmonitors", "false");
		options.put("decodeenumswitch", "true");
		options.put("decodefinally", "true");
		options.put("decodelambdas", "true");
		options.put("decodestringswitch", "true");
		options.put("dumpclasspath", "false");
		options.put("eclipse", "true");
		options.put("elidescala", "false");
		options.put("forcecondpropagate", "false");
		options.put("forceexceptionprune", "false");
		options.put("forcereturningifs", "false");
		options.put("forcetopsort", "true");
		options.put("forcetopsortaggress", "true");
		options.put("forloopaggcapture", "false");
		options.put("hidebridgemethods", "true");
		options.put("hidelangimports", "true");
		options.put("hidelongstrings", "true");
		options.put("hideutf", "false");
		options.put("ignoreexceptionsalways", "true");
		options.put("innerclasses", "true");
		options.put("j14classobj", "false");
		options.put("labelledblocks", "true");
		options.put("lenient", "false");
		options.put("liftconstructorinit", "true");
		options.put("override", "true");
		options.put("pullcodecase", "false");
		options.put("recover", "true");
		options.put("recovertypeclash", "false");
		options.put("recovertypehints", "false");
		options.put("relinkconststring", "true");
		options.put("removebadgenerics", "true");
		options.put("removeboilerplate", "true");
		options.put("removedeadmethods", "true");
		options.put("removeinnerclasssynthetics", "false");
		options.put("rename", "false");
		options.put("renamedupmembers", "false");
		options.put("renameenumidents", "false");
		options.put("renameillegalidents", "false");
		options.put("showinferrable", "false");
		options.put("showversion", "false");
		options.put("silent", "false");
		options.put("stringbuffer", "true");
		options.put("stringbuilder", "true");
		options.put("sugarasserts", "true");
		options.put("sugarboxing", "true");
		options.put("sugarenums", "true");
		options.put("tidymonitors", "true");
		options.put("commentmonitors", "false");
		options.put("tryresources", "true");
		options.put("usenametable", "true");
		options.put("analyseas", "CLASS");
	}

	private static String decompiled;

	public static String decompile(ClassNode cn) {
		try {
			byte[] b = Conversion.toBytecode0(cn);
			decompiled = null;
			OutputSinkFactory mySink = new OutputSinkFactory() {
				@Override
				public List<SinkClass> getSupportedSinks(SinkType sinkType, Collection<SinkClass> collection) {
					if (sinkType == SinkType.JAVA && collection.contains(SinkClass.DECOMPILED)) {
						return Arrays.asList(SinkClass.DECOMPILED, SinkClass.STRING);
					} else {
						return Collections.singletonList(SinkClass.STRING);
					}
				}

				Consumer<SinkReturns.Decompiled> dumpDecompiled = d -> {
					decompiled = d.getJava().substring(31);
				};

				@Override
				public <T> Sink<T> getSink(SinkType sinkType, SinkClass sinkClass) {
					if (sinkType == SinkType.JAVA && sinkClass == SinkClass.DECOMPILED) {
						return x -> dumpDecompiled.accept((SinkReturns.Decompiled) x);
					}
					return ignore -> {
					};
				}
			};
			ClassFileSource2 cfs2 = new ClassFileSource2() {
				@Override
				public void informAnalysisRelativePathDetail(String a, String b) {
				}

				@Override
				public String getPossiblyRenamedPath(String path) {
					return path;
				}

				@Override
				public Pair<byte[], String> getClassFileContent(String path) throws IOException {
					String name = path.substring(0, path.length() - 6);
					if (name.equals(cn.name)) {
						return Pair.make(b, name);
					}
					ClassNode dummy = new ClassNode();
					dummy.name = name;
					dummy.version = 52;
					return Pair.make(Conversion.toBytecode0(dummy), name); // cfr loads unnecessary classes
				}

				@Override
				public Collection<String> addJar(String arg0) {
					throw new RuntimeException();
				}

				@Override
				public JarContent addJarContent(String jarPath, AnalysisType analysisType) {
					return null;
				}
			};
			CfrDriver cfrDriver = new CfrDriver.Builder().withClassFileSource(cfs2).withOutputSink(mySink).withOptions(options).build();
			cfrDriver.analyse(Arrays.asList(cn.name));
		} catch (Exception e) {
			e.printStackTrace();
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			return sw.toString();
		}
		return decompiled;
	}
}