<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>유저 목록 관리</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 20px;
        }
        
        .counter-container {
            counter-reset: item-counter;
        }
        
        .counter-item {
            counter-increment: item-counter;
        }
        
        .counter-item::before {
            content: counter(item-counter) ". ";
            font-weight: bold;
            color: #007bff;
            margin-right: 5px;
        }
        
        .controls {
            margin: 20px 0;
        }
        
        .user-list {
            max-height: 600px;
            overflow-y: auto;
            border: 1px solid #ddd;
            padding: 15px;
            border-radius: 5px;
        }
        
        .user-item {
            padding: 10px;
            margin: 5px 0;
            border: 1px solid #eee;
            border-radius: 4px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            background: #f9f9f9;
        }
        
        .user-info {
            flex: 1;
        }
        
        .delete-btn {
            padding: 6px 12px;
            background: #dc3545;
            color: white;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            font-size: 12px;
        }
        
        .delete-btn:hover {
            background: #c82333;
        }
        
        button {
            padding: 10px 20px;
            margin: 5px;
            background: #007bff;
            color: white;
            border: none;
            border-radius: 4px;
            cursor: pointer;
        }
        
        button:hover {
            background: #0056b3;
        }
        
        .performance-info {
            background: #f8f9fa;
            padding: 15px;
            border-radius: 5px;
            margin: 15px 0;
        }
        
        .performance-info p {
            margin: 5px 0;
        }
    </style>
</head>
<body>
    <h1>유저 목록 관리 시스템</h1>
    
    <div class="controls">
        <button onclick="loadUsers(10000)">유저 10000명 로드</button>
        <button onclick="clearUsers()">초기화</button>
    </div>
    
    <div id="performance-info" class="performance-info">
        <p>로드된 유저 수: <span id="user-count">0</span></p>
        <p>API 호출 시간: <span id="api-time">0</span>ms</p>
        <p>렌더링 시간: <span id="render-time">0</span>ms</p>
        <p>총 시간: <span id="total-time">0</span>ms</p>
    </div>
    
    <div id="user-list" class="user-list counter-container">
        <!-- 유저 데이터가 표시될 영역 -->
    </div>
    
    <script>
        async function loadUsers(limit) {
            // 10000개 제한
            if (limit > 10000) {
                alert('최대 10000명까지만 로드할 수 있습니다.');
                return;
            }
            
            const totalStartTime = performance.now();
            
            try {
                // API 호출 시작
                const apiStartTime = performance.now();
                const response = await fetch(`/api/users/all?limit=${limit}&sortBy=id&sortDir=asc`);
                
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                
                const data = await response.json();
                console.log('API 응답 데이터:', data);
                console.log('첫 번째 유저:', data.users[0]);
                const apiEndTime = performance.now();
                const apiTime = (apiEndTime - apiStartTime).toFixed(2);
                
                // 렌더링 시작
                const renderStartTime = performance.now();
                const container = document.getElementById('user-list');
                container.innerHTML = '';
                
                // 유저 데이터 렌더링
                data.users.forEach((user, index) => {
                    console.log('렌더링할 유저:', user);
                    const userDiv = document.createElement('div');
                    userDiv.className = 'counter-item user-item';
                    userDiv.innerHTML = 
                        '<div class="user-info">' +
                            '<strong>' + user.username + '</strong> - ' + user.email + ' (ID: ' + user.id + ')' +
                        '</div>' +
                        '<button class="delete-btn" onclick="deleteUser(' + user.id + ', this)">삭제</button>';
                    container.appendChild(userDiv);
                });
                
                const renderEndTime = performance.now();
                const renderTime = (renderEndTime - renderStartTime).toFixed(2);
                
                const totalEndTime = performance.now();
                const totalTime = (totalEndTime - totalStartTime).toFixed(2);
                
                // 성능 정보 업데이트
                document.getElementById('user-count').textContent = data.users.length;
                document.getElementById('api-time').textContent = apiTime;
                document.getElementById('render-time').textContent = renderTime;
                document.getElementById('total-time').textContent = totalTime;
                
                console.log(`유저 ${data.users.length}명 로드 완료:`);
                console.log(`- API 호출: ${apiTime}ms`);
                console.log(`- 렌더링: ${renderTime}ms`);
                console.log(`- 총 시간: ${totalTime}ms`);
                
            } catch (error) {
                console.error('유저 로드 실패:', error);
                alert('유저 데이터 로드에 실패했습니다: ' + error.message);
            }
        }
        
        async function deleteUser(userId, buttonElement) {
            console.log('삭제할 유저 ID:', userId);
            const url = '/api/users/' + userId;
            console.log('요청 URL:', url);
            
            try {
                const response = await fetch(url, {
                    method: 'DELETE'
                });
                
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                
                // UI에서 해당 유저 제거
                const userItem = buttonElement.closest('.user-item');
                userItem.remove();
                
                // 카운트 업데이트
                const currentCount = parseInt(document.getElementById('user-count').textContent);
                document.getElementById('user-count').textContent = currentCount - 1;
                
                console.log(`유저 ${userId} 삭제 완료`);
                
            } catch (error) {
                console.error('유저 삭제 실패:', error);
                alert('유저 삭제에 실패했습니다: ' + error.message);
            }
        }
        
        function clearUsers() {
            document.getElementById('user-list').innerHTML = '';
            document.getElementById('user-count').textContent = '0';
            document.getElementById('api-time').textContent = '0';
            document.getElementById('render-time').textContent = '0';
            document.getElementById('total-time').textContent = '0';
        }
        
        // 페이지 로드시 10000명 자동 로드
        window.onload = function() {
            loadUsers(10000);
        };
    </script>
</body>
</html>