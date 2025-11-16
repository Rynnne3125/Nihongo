from flask import Flask, request, jsonify
from flask_cors import CORS
import os
import requests
import cloudinary
import cloudinary.uploader
import base64
import google.generativeai as genai
import json
from datetime import datetime
from dotenv import load_dotenv
import edge_tts
import asyncio
import re

# Load environment variables
load_dotenv()

# === Assistant imports ===
try:
    # Import core dependencies
    import pygame
    ASSISTANT_AVAILABLE = True
except ImportError as e:
    print(f"⚠️ Assistant dependencies not available: {e}")
    ASSISTANT_AVAILABLE = False

# Initialize Flask app
app = Flask(__name__)
CORS(app)

# === Configuration ===
# Cloudinary configuration (Giữ nguyên để upload ảnh/tài liệu học tập)
cloudinary.config(
    cloud_name=os.getenv("CLOUDINARY_CLOUD_NAME", "ddjrbkhpx"),
    api_key=os.getenv("CLOUDINARY_API_KEY", "534297453884984"),
    api_secret=os.getenv("CLOUDINARY_API_SECRET", "23OLY_AqI11rISnQ5EHl66OHahU")
)

# Gemini AI configuration
GEMINI_API_KEY = os.getenv("GEMINI_API_KEY", "AIzaSyBGWplwpUQUIUZ9QAg3dPMj5poFeNr1qu8")
genai.configure(api_key=GEMINI_API_KEY)
gemini_model = genai.GenerativeModel("gemini-2.0-flash")

# === Memory Management ===
conversation_memory = {
    'context': [], # Lưu lịch sử chat
    'current_level': 'N5' # Mặc định trình độ
}

def clean_text(text):
    return re.sub(r"[*_`>#+-]", "", text).strip()

# === Edge TTS: Hỗ trợ cả Tiếng Nhật và Tiếng Việt ===
async def generate_audio_base64(text, lang="ja"):
    """
    Tạo file âm thanh base64.
    lang='ja': Giọng Nhật (Nanami) - Dùng để đọc từ vựng/câu mẫu
    lang='vi': Giọng Việt (HoaiMy) - Dùng để giải thích
    """
    voice = "ja-JP-NanamiNeural" if lang == "ja" else "vi-VN-HoaiMyNeural"

    from io import BytesIO
    mp3_fp = BytesIO()
    communicate = edge_tts.Communicate(text, voice)
    async for chunk in communicate.stream():
        if chunk["type"] == "audio":
            mp3_fp.write(chunk["data"])
    mp3_fp.seek(0)
    audio_base64 = base64.b64encode(mp3_fp.read()).decode('utf-8')
    return audio_base64

# === AI Logic cho Tiếng Nhật ===

def analyze_japanese_content(text_content):
    """
    Phân tích đoạn văn bản tiếng Nhật để tách từ vựng, kanji và ngữ pháp.
    Dùng cho tính năng: Chụp ảnh/Upload PDF -> Tạo bài học tự động.
    """
    try:
        prompt = f"""
        Bạn là trợ lý học tiếng Nhật chuyên nghiệp (Sensei). 
        Hãy phân tích đoạn văn bản tiếng Nhật sau đây:
        
        "{text_content}"
        
        Hãy trả về kết quả dưới dạng JSON format chuẩn với cấu trúc sau:
        {{
            "summary_vi": "Tóm tắt nội dung bằng tiếng Việt",
            "vocabulary": [
                {{"word": "tiếng nhật", "reading": "hiragana/romaji", "meaning": "nghĩa tiếng việt"}}
            ],
            "kanji": [
                {{"character": "Hán tự", "onyomi": "âm on", "kunyomi": "âm kun", "meaning": "nghĩa Hán Việt"}}
            ],
            "grammar_points": [
                {{"structure": "Cấu trúc ngữ pháp", "explanation": "Giải thích ngắn gọn", "example": "Ví dụ trong bài"}}
            ]
        }}
        Chỉ trả về JSON, không thêm text dẫn dắt.
        """

        response = gemini_model.generate_content(prompt)
        response_text = response.text.strip()

        # Clean json formatting if exists
        if "```json" in response_text:
            start = response_text.find("```json") + 7
            end = response_text.find("```", start)
            response_text = response_text[start:end].strip()

        return json.loads(response_text)
    except Exception as e:
        print(f"Error analyzing Japanese: {e}")
        return None

def get_sensei_reply(user_text):
    """
    Chatbot đóng vai Sensei, giải thích thắc mắc hoặc luyện hội thoại.
    """
    try:
        # Lấy lịch sử chat gần nhất
        recent_context = "\n".join(conversation_memory['context'][-5:])

        prompt = f"""
        Bạn là Nihongo Sensei - một trợ lý AI giúp người Việt học tiếng Nhật.
        
        NGUYÊN TẮC:
        1. Nếu người dùng hỏi về ngữ pháp/từ vựng: Giải thích chi tiết bằng tiếng Việt, đưa ra ví dụ (có Furigana hoặc Romaji).
        2. Nếu người dùng chào hoặc chat bằng tiếng Nhật: Hãy đóng vai người bản xứ để luyện hội thoại (Kaiwa).
        3. Luôn thân thiện, khuyến khích người học.
        4. Nếu câu tiếng Nhật của người dùng sai, hãy nhẹ nhàng sửa lại (Correction).

        Lịch sử chat:
        {recent_context}

        Người dùng: "{user_text}"
        
        Trả lời:
        """

        response = gemini_model.generate_content(prompt)
        reply = response.text.strip()

        # Lưu vào bộ nhớ
        conversation_memory['context'].append(f"User: {user_text}")
        conversation_memory['context'].append(f"Sensei: {reply}")

        return reply
    except Exception as e:
        return "Sensei đang bận chút, em thử lại sau nhé! (Lỗi kết nối AI)"

# === Routes ===

@app.route('/', methods=['GET'])
def root():
    return jsonify({
        'message': 'Nihongo App AI Server',
        'status': 'running',
        'features': ['chat', 'analyze_text', 'tts']
    })

@app.route('/chat', methods=['POST'])
def chat():
    """Endpoint chat với AI Sensei"""
    try:
        data = request.get_json()
        user_text = data.get('message', '')

        if not user_text:
            return jsonify({'error': 'No message provided'}), 400

        # 1. Lấy phản hồi từ Gemini
        reply = get_sensei_reply(user_text)

        # 2. Tạo audio (Mặc định giọng Việt để giải thích,
        # logic phức tạp hơn có thể detect ngôn ngữ để switch giọng)
        audio_base64 = None

        # Đơn giản hóa: Nếu phản hồi chứa nhiều ký tự Kana/Kanji -> đọc tiếng Nhật, ngược lại đọc tiếng Việt
        jp_char_count = len(re.findall(r'[\u3040-\u30ff\u4e00-\u9faf]', reply))
        lang_mode = "ja" if jp_char_count > len(reply) * 0.3 else "vi"

        try:
            audio_base64 = asyncio.run(generate_audio_base64(clean_text(reply), lang=lang_mode))
        except Exception as e:
            print(f"Audio gen failed: {e}")

        return jsonify({
            'reply': reply,
            'audio': audio_base64,
            'lang_detected': lang_mode
        })
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@app.route('/analyze', methods=['POST'])
def analyze_text():
    """
    Endpoint quan trọng: Nhận văn bản (từ OCR trên Android gửi lên)
    và trả về phân tích từ vựng/ngữ pháp để tạo bài học tức thì.
    """
    try:
        data = request.get_json()
        japanese_text = data.get('text', '')

        if not japanese_text:
            return jsonify({'error': 'No text provided'}), 400

        analysis_result = analyze_japanese_content(japanese_text)

        if not analysis_result:
            return jsonify({'error': 'Could not analyze text'}), 500

        return jsonify(analysis_result)

    except Exception as e:
        return jsonify({'error': str(e)}), 500

@app.route('/tts', methods=['POST'])
def text_to_speech():
    """Endpoint chuyên biệt để đọc mẫu câu tiếng Nhật"""
    try:
        data = request.get_json()
        text = data.get('text', '')
        lang = data.get('lang', 'ja') # Mặc định là tiếng Nhật

        audio = asyncio.run(generate_audio_base64(text, lang))
        return jsonify({'audio': audio})
    except Exception as e:
        return jsonify({'error': str(e)}), 500

if __name__ == '__main__':
    port = int(os.environ.get('PORT', 5001))
    print("🇯🇵 Nihongo AI Server Starting...")
    app.run(host='0.0.0.0', port=port, debug=True)