document.addEventListener('DOMContentLoaded', function() {
    // 从 URL 获取 testId
    const testId = new URLSearchParams(window.location.search).get('testId');
    let failureData = [];

    if (testId) {
        // 加载测试基本信息
        loadTestInfo(testId);

        // 加载所有失败记录
        loadFailures(testId);

        // 返回详情页面按钮
        const backToDetailBtn = document.getElementById('back-to-detail-btn');
        if (backToDetailBtn) {
            backToDetailBtn.addEventListener('click', () => {
                window.location.href = `detail.html?id=${testId}`;
            });
        }
    } else {
        // 没有提供 testId，显示错误信息
        document.body.innerHTML = '<div class="container"><h1>错误</h1><p>未指定测试 ID，无法加载失败请求列表。</p><a href="index.html">返回首页</a></div>';
    }

    // 加载测试基本信息
    function loadTestInfo(testId) {
        fetch(`/api/tests/${testId}`)
            .then(response => response.json())
            .then(result => {
                const test = result.data;
                document.getElementById('test-name').textContent = test.name;
                document.getElementById('test-summary').textContent =
                    `开始时间: ${new Date(test.startTime).toLocaleString()} | ` +
                    `总请求数: ${test.totalRequests} | ` +
                    `失败请求数: ${test.failedRequests}`;
            })
            .catch(error => console.error('加载测试信息失败:', error));
    }

    // 加载失败请求列表 - 移除分页功能
    function loadFailures(testId) {
        fetch(`/api/tests/${testId}/failures`)
            .then(response => response.json())
            .then(result => {
                if (result.success && result.data) {
                    failureData = result.data;

                    if (failureData.length === 0) {
                        document.getElementById('failures-container').style.display = 'none';
                        document.getElementById('no-failures').style.display = 'block';
                    } else {
                        document.getElementById('failures-container').style.display = 'block';
                        document.getElementById('no-failures').style.display = 'none';
                        renderFailures();
                    }
                } else {
                    console.error('加载失败请求列表失败:', result.msg);
                }
            })
            .catch(error => console.error('加载失败请求列表失败:', error));
    }

    // 渲染失败请求列表 - 显示所有数据
    function renderFailures() {
        const failuresListElement = document.getElementById('failures-list');
        failuresListElement.innerHTML = '';

        failureData.forEach(failure => {
            const row = document.createElement('tr');

            // 计算请求耗时
            const duration = failure.endTime - failure.startTime;

            // 格式化时间
            const date = new Date(failure.startTime);
            const formattedTime = date.toLocaleString();

            // 截断过长的响应内容
            let shortResponse = failure.responseBody;
            let expandButton = '';
            if (failure.responseBody && failure.responseBody.length > 100) {
                shortResponse = failure.responseBody.substring(0, 100) + '...';
                expandButton = '<span class="expand-btn" data-full-response="' +
                    failure.responseBody.replace(/"/g, '&quot;') +
                    '">查看完整响应</span>';
            }

            row.innerHTML = `
                <td>${formattedTime}</td>
                <td>${failure.url}</td>
                <td>${failure.httpCode}</td>
                <td class="response-cell">${shortResponse}${expandButton}</td>
                <td>${duration} ms</td>
            `;

            failuresListElement.appendChild(row);
        });

        // 添加展开响应内容的事件监听器
        document.querySelectorAll('.expand-btn').forEach(btn => {
            btn.addEventListener('click', function() {
                alert(this.getAttribute('data-full-response'));
            });
        });
    }
});