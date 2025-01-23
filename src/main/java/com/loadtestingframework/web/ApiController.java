package com.loadtestingframework.web;

import com.loadtestingframework.entity.LoadTest;
import com.loadtestingframework.entity.TestMetrics;
import com.loadtestingframework.service.JobExecuteService;
import com.loadtestingframework.service.LoadTestService;
import com.loadtestingframework.web.request.CreateTestRequest;
import com.loadtestingframework.web.viewobject.CommonVO;
import com.loadtestingframework.web.viewobject.JobScriptVO;
import com.loadtestingframework.web.viewobject.LoadTestDetailVO;
import com.loadtestingframework.web.viewobject.LoadTestVO;
import erp.ERP;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private LoadTestService loadTestService;

    @Autowired
    private JobExecuteService jobExecuteService;

    @RequestMapping("/tests")
    @ResponseBody
    public CommonVO getAllTests() {
        List<LoadTest> loadTests = loadTestService.getAllTests();
        return CommonVO.success(LoadTestVO.fromLoadTests(loadTests));
    }

    @PostMapping("/tests")
    @ResponseBody
    public CommonVO createTest(@RequestBody CreateTestRequest request) {
        if (request.getSubmitMethod().equals("full")) {
            loadTestService.createTest(request.getName(), request.getScriptId(), request.getJobCount(),
                    request.getDescription(), System.currentTimeMillis());
        } else if (request.getSubmitMethod().equals("gradual")) {
            loadTestService.createGraduallyTest(request.getName(), request.getScriptId(), request.getJobCount(),
                    request.getIncrement(), request.getInterval() * 1000L, request.getDescription(), System.currentTimeMillis());
        }
        return CommonVO.success();
    }

    //DELETE /api/tests/{testId}：删除指定测试
    @DeleteMapping("/tests/{testId}")
    @ResponseBody
    public CommonVO deleteTest(@PathVariable String testId) {
        loadTestService.deleteTest(testId);
        return CommonVO.success();
    }

    //GET /api/tests/{testId}：获取指定测试的详情
    @GetMapping("/tests/{testId}")
    @ResponseBody
    public CommonVO getTest(@PathVariable String testId) {
        LoadTest loadTest = loadTestService.getLoadTest(testId);
        TestMetrics testMetrics = loadTestService.getTestMetrics(testId);
        return CommonVO.success(new LoadTestDetailVO(loadTest, testMetrics));
    }

    //POST /api/tests/{testId}/stop：停止指定测试
    @PostMapping("/tests/{testId}/stop")
    @ResponseBody
    public CommonVO stopTest(@PathVariable String testId) {
        ERP.retry(() -> loadTestService.stopTest(testId), 4, 100);
        return CommonVO.success();
    }

    //GET /api/scripts：获取所有测试脚本
    @GetMapping("/scripts")
    @ResponseBody
    public CommonVO getAllScripts() {
        List<String> allScripts = jobExecuteService.getAllScripts();
        return CommonVO.success(JobScriptVO.fromJobScripts(allScripts));
    }

    //导出CSV
    @GetMapping("/export-csv")
    public ResponseEntity<byte[]> exportCsv() throws IOException {
        List<LoadTest> loadTests = loadTestService.getAllTests();
        List<String[]> data = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (LoadTest test : loadTests) {
            TestMetrics testMetrics = loadTestService.getTestMetrics(test.getName());
            if (testMetrics == null) {
                continue;
            }
            LoadTestDetailVO detailVO = new LoadTestDetailVO(test, testMetrics);
            Date dateForStartTime = new Date(detailVO.getStartTime());
            String startTimeString = sdf.format(dateForStartTime);
            data.add(new String[]{
                    detailVO.getName(),
                    detailVO.getJobScriptName(),
                    String.valueOf(detailVO.getCurrentJobCount()),
                    String.valueOf(detailVO.getSetJobCount()),
                    String.valueOf(detailVO.getRps()),
                    String.valueOf(detailVO.getAvgLatency()),
                    String.valueOf(detailVO.getFailedRequests()),
                    String.valueOf(detailVO.getTotalRequests()),
                    detailVO.getDescription(),
                    startTimeString
            });
        }

        // 创建 CSV 文件
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, Charset.forName("GBK"));
        CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                .withHeader("测试名称", "测试脚本", "当前作业数量", "设定作业数量", "RPS", "平均延时",
                        "失败请求数", "总请求数", "说明", "开始时间"));

        for (String[] row : data) {
            csvPrinter.printRecord(row);
        }

        csvPrinter.flush();
        csvPrinter.close();

        // 返回 CSV 文件
        byte[] csvBytes = outputStream.toByteArray();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=tests.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvBytes);
    }
}
