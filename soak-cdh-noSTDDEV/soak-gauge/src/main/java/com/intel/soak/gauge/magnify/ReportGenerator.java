package com.intel.soak.gauge.magnify;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import com.intel.soak.*;
import com.intel.soak.config.ConfigWriter;
import com.intel.soak.gauge.GangliaReport;
import com.intel.soak.gauge.GaugeChart;
import com.intel.soak.gauge.GaugeMetrics;
import com.intel.soak.gauge.GaugeReport;
import com.intel.soak.gauge.storage.impl.InMemSource;
import com.intel.soak.model.MergeConfig;
import com.intel.soak.utils.DateTimeUtils;
import com.intel.soak.utils.FileUtils;

public class ReportGenerator {

    private InputStream genJobReport(JobReport job) {
        HashMap<String, Object> vars = new HashMap<String, Object>();
        vars.put("job", job);
        vars.put("genDate", DateTimeUtils.millisToStdTimeString(System.currentTimeMillis()));
        return new ByteArrayInputStream(FileUtils.applyTemplate("job_report.vm", vars).getBytes());
    }

    private InputStream genGaugeReport(GaugeReport report) {
        HashMap<String, Object> vars = new HashMap<String, Object>();
        vars.put("report", report);
        vars.put("genDate", DateTimeUtils.millisToStdTimeString(System.currentTimeMillis()));
        return new ByteArrayInputStream(FileUtils.applyTemplate("report.vm", vars).getBytes());
    }

    private void appendJarFiles(JarOutputStream jos, JobReport job) throws Exception {
        InMemSource source = new InMemSource();
        for(GaugeMetrics m : job.getDetailMetrics()){
            source.append(m);
        }
        JarEntry entry = new JarEntry(job.getName() + "/");
        entry.setTime(System.currentTimeMillis());
        jos.putNextEntry(entry);
        jos.closeEntry();

        entry = new JarEntry(job.getName() + "/detail_metrics.csv");
        entry.setTime(System.currentTimeMillis());
        jos.putNextEntry(entry);
        InputStream ins = source.getInputStream();
        FileUtils.copyStream(ins, jos);
        ins.close();
        jos.closeEntry();

        for(GaugeChart chart : job.getCharts()){
            entry = new JarEntry(job.getName() + "/" + chart.getName() + ".jpg");
            entry.setTime(System.currentTimeMillis());
            jos.putNextEntry(entry);
            ins = new ByteArrayInputStream(chart.getContent());
            FileUtils.copyStream(ins, jos);
            ins.close();
            jos.closeEntry();
        }

        entry = new JarEntry(job.getName() + "/job_report.html");
        entry.setTime(System.currentTimeMillis());
        jos.putNextEntry(entry);
        ins = genJobReport(job);
        FileUtils.copyStream(ins, jos);
        jos.closeEntry();
        ins.close();

        entry = new JarEntry(job.getName() + "/merge.xml");
        entry.setTime(System.currentTimeMillis());
        jos.putNextEntry(entry);
        ins = ConfigWriter.toInputStream(MergeConfig.class, job.getConfig());
        FileUtils.copyStream(ins, jos);
        jos.closeEntry();
        ins.close();
    }

    private void appendJarFiles(JarOutputStream jos, GangliaReport gangliaReport) throws Exception{
        if(gangliaReport == null)
            return;
        InMemSource source = new InMemSource();
        for(GaugeMetrics m : gangliaReport.getGangliaMetrics()){
            source.append(m);
        }
        JarEntry entry = new JarEntry("ganglia/");
        entry.setTime(System.currentTimeMillis());
        jos.putNextEntry(entry);
        jos.closeEntry();

        entry = new JarEntry("ganglia/metrics.csv");
        entry.setTime(System.currentTimeMillis());
        jos.putNextEntry(entry);
        InputStream ins = source.getInputStream();
        FileUtils.copyStream(ins, jos);
        ins.close();
        jos.closeEntry();

        for(GaugeChart chart : gangliaReport.getCharts()){
            entry = new JarEntry("ganglia/" + chart.getName() + ".jpg");
            entry.setTime(System.currentTimeMillis());
            jos.putNextEntry(entry);
            ins = new ByteArrayInputStream(chart.getContent());
            FileUtils.copyStream(ins, jos);
            ins.close();
            jos.closeEntry();
        }
    }

    public ByteArrayInputStream createArchive(GaugeReport report) throws Exception{
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        JarOutputStream jos = new JarOutputStream(bos);
        for(JobReport job : report.getJobReports()){
            appendJarFiles(jos, job);
        }
        appendJarFiles(jos, report.getGangliaReport());
        JarEntry entry = new JarEntry("report.html");
        entry.setTime(System.currentTimeMillis());
        jos.putNextEntry(entry);
        InputStream ins = genGaugeReport(report);
        FileUtils.copyStream(ins, jos);
        jos.closeEntry();
        ins.close();
        jos.flush();
        jos.close();
        return new ByteArrayInputStream(bos.toByteArray());
    }
}
