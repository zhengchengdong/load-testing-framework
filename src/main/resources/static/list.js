document.addEventListener('DOMContentLoaded', function() {
    const newTestBtn = document.getElementById('new-test-btn');
    const modal = document.getElementById('new-test-modal');
    const closeModal = document.querySelector('.close');
    const newTestForm = document.getElementById('new-test-form');
    const submitMethodRadios = document.querySelectorAll('input[name="submit-method"]');
    const gradualOptions = document.getElementById('gradual-options');

    if (newTestBtn && modal && closeModal && newTestForm) {
        // 显示新增测试弹窗
        newTestBtn.addEventListener('click', () => {
            modal.style.display = 'block';
            fetch('/api/scripts')
                .then(response => response.json())
                .then(result => {
                    const scripts = result.data; // 获取 data.data
                    const scriptSelect = document.getElementById('test-script');
                    scriptSelect.innerHTML = '';
                    scripts.forEach(script => {
                        const option = document.createElement('option');
                        option.value = script.id;
                        option.textContent = script.name;
                        scriptSelect.appendChild(option);
                    });
                })
                .catch(error => console.error('加载脚本失败:', error));
        });

        // 关闭弹窗
        closeModal.addEventListener('click', () => {
            modal.style.display = 'none';
        });

        // 提交新增测试表单
        newTestForm.addEventListener('submit', function(event) {
            event.preventDefault();
            const formData = new FormData(this);
            const data = {
                name: formData.get('test-name'),
                scriptId: formData.get('test-script'),
                jobCount: parseInt(formData.get('job-count')),
                submitMethod: formData.get('submit-method'),
                interval: formData.get('interval') ? parseInt(formData.get('interval')) : null,
                increment: formData.get('increment') ? parseInt(formData.get('increment')) : null,
                description: formData.get('description')
            };
            fetch('/api/tests', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data)
            })
                .then(response => response.json())
                .then(result => {
                    if (result.success) {
                        modal.style.display = 'none';
                        loadTestList();
                    } else {
                        console.error('创建测试失败:', result.msg); // 使用 result.msg
                    }
                })
                .catch(error => console.error('创建测试失败:', error));
        });

        // 显示/隐藏逐渐增加选项
        submitMethodRadios.forEach(radio => {
            radio.addEventListener('change', () => {
                gradualOptions.style.display = radio.value === 'gradual' ? 'block' : 'none';
            });
        });
    }

    // 加载测试列表
    function loadTestList() {
        fetch('/api/tests')
            .then(response => response.json())
            .then(result => {
                const tests = result.data; // 获取 data.data
                const testTableBody = document.getElementById('test-table-body');
                if (testTableBody) {
                    testTableBody.innerHTML = '';
                    tests.forEach(test => {
                        const row = document.createElement('tr');
                        row.innerHTML = `
                            <td>${test.name}</td>
                            <td>${test.scriptName}</td>
                            <td>${test.jobCount}</td>
                            <td>${test.submitMethod === 'full' ? '全量' : '逐渐增加'}</td>
                            <td>${test.startTime ? new Date(test.startTime).toLocaleString() : ''}</td>
                            <td>${test.description}</td>
                            <td>
                                <button class="detail-btn">详情</button>
                                <button class="delete-btn">删除</button>
                            </td>
                        `;

                        // 绑定详情按钮的点击事件
                        const detailBtn = row.querySelector('.detail-btn');
                        detailBtn.addEventListener('click', () => {
                            window.location.href = `detail.html?id=${test.id}`;
                        });

                        // 绑定删除按钮的点击事件
                        const deleteBtn = row.querySelector('.delete-btn');
                        deleteBtn.addEventListener('click', () => {
                            deleteTest(test.id);
                        });

                        testTableBody.appendChild(row);
                    });
                }
            })
            .catch(error => console.error('加载测试列表失败:', error));
    }

    // 删除测试
    function deleteTest(testId) {
        fetch(`/api/tests/${testId}`, { method: 'DELETE' })
            .then(response => response.json())
            .then(result => {
                if (result.success) {
                    loadTestList(); // 刷新列表
                } else {
                    console.error('删除测试失败:', result.msg); // 使用 result.msg
                }
            })
            .catch(error => console.error('删除测试失败:', error));
    }

    // 初始加载测试列表
    loadTestList();
});