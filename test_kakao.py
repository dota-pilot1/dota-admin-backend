import requests

# 카카오톡 API 테스트
api_key = "43c20444b1aa6226e3c76dccc4b52ea7"

# 나에게 보내기 API 테스트
url = "https://kapi.kakao.com/v2/api/talk/memo/default/send"
headers = {
    "Authorization": f"Bearer {api_key}",
    "Content-Type": "application/x-www-form-urlencoded"
}

template_object = """{
    "object_type": "text",
    "text": "회원가입 테스트 메시지입니다!",
    "link": {
        "web_url": "https://developers.kakao.com",
        "mobile_web_url": "https://developers.kakao.com"
    }
}"""

data = {
    "template_object": template_object
}

response = requests.post(url, headers=headers, data=data)
print(f"Status Code: {response.status_code}")
print(f"Response: {response.text}")