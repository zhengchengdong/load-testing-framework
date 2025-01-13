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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
                    System.currentTimeMillis());
        } else if (request.getSubmitMethod().equals("gradual")) {
            loadTestService.createGraduallyTest(request.getName(), request.getScriptId(), request.getJobCount(),
                    request.getIncrement(), request.getInterval(), System.currentTimeMillis());
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
        int currentJobCount = jobExecuteService.getJobCount(testId);
        return CommonVO.success(new LoadTestDetailVO(loadTest, testMetrics, currentJobCount));
    }

    //POST /api/tests/{testId}/stop：停止指定测试
    @PostMapping("/tests/{testId}/stop")
    @ResponseBody
    public CommonVO stopTest(@PathVariable String testId) {
        loadTestService.stopTest(testId);
        return CommonVO.success();
    }

    //GET /api/scripts：获取所有测试脚本
    @GetMapping("/scripts")
    @ResponseBody
    public CommonVO getAllScripts() {
        List<String> allScripts = jobExecuteService.getAllScripts();
        return CommonVO.success(JobScriptVO.fromJobScripts(allScripts));
    }

}
