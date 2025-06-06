document.addEventListener('DOMContentLoaded', function() {
    const testId = new URLSearchParams(window.location.search).get('id');
    if (testId) {
        function loadTestDetail() {
            fetch(`/api/tests/${testId}`)
                .then(response => response.json())
                .then(result => {
                    const test = result.data; // 获取 data.data
                    document.getElementById('test-name').textContent = test.name;
                    document.getElementById('job-script-name').textContent = test.jobScriptName;
                    document.getElementById('current-job-count').textContent = test.currentJobCount;
                    document.getElementById('set-job-count').textContent = test.setJobCount;
                    document.getElementById('total-requests').textContent = test.totalRequests;
                    document.getElementById('rps').textContent = test.rps;
                    document.getElementById('avg-latency').textContent = test.avgLatency;

                    // 获取失败请求链接元素
                    const failedRequestsElement = document.getElementById('failed-requests');
                    // 设置链接的文本内容
                    failedRequestsElement.textContent = test.failedRequests;
                    // 动态设置链接的href，加入testId参数
                    failedRequestsElement.href = `failed-requests.html?testId=${testId}`;

                    document.getElementById('start-time').textContent = test.startTime ? new Date(test.startTime).toLocaleString() : '';
                    document.getElementById('description').textContent = test.description;
                })
                .catch(error => console.error('加载测试详情失败:', error));
        }

        // 每秒刷新测试详情
        loadTestDetail();
        setInterval(loadTestDetail, 1000);

        // 停止测试
        const stopTestBtn = document.getElementById('stop-test-btn');
        if (stopTestBtn) {
            stopTestBtn.addEventListener('click', () => {
                fetch(`/api/tests/${testId}/stop`, { method: 'POST' })
                    .then(response => response.json())
                    .then(result => {
                        if (result.success) {
                            loadTestDetail();
                        } else {
                            console.error('停止测试失败:', result.msg); // 使用 result.msg
                        }
                    })
                    .catch(error => console.error('停止测试失败:', error));
            });
        }
    }

    // 返回列表按钮
    const backToListBtn = document.getElementById('back-to-list-btn');
    if (backToListBtn) {
        backToListBtn.addEventListener('click', () => {
            window.location.href = 'index.html'; // 跳转到列表页
        });
    }
});