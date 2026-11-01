#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Cong cu nhan file log tu Bang dieu khien phong mo Sin Ho - Lai Chau (Tinker Board).
Chay bang Python 3 tieu chuan (Khong can cai dat bat ky thu vien ngoai nao).
"""

import os
import sys
import socket
from datetime import datetime
from http.server import HTTPServer, BaseHTTPRequestHandler

# Dam bao tuong thich ma hoa tren Windows Console
if sys.platform.startswith("win"):
    try:
        sys.stdout.reconfigure(encoding="utf-8", errors="replace")
        sys.stderr.reconfigure(encoding="utf-8", errors="replace")
    except Exception:
        pass

PORT = 8080
SAVE_DIR = os.path.join(os.path.dirname(os.path.abspath(__file__)), "received_logs")

def get_all_ip_addresses():
    """Lay danh sach tat ca IP noi bo (LAN / Wi-Fi / Tailscale) cua may tinh."""
    ip_list = []
    try:
        hostname = socket.gethostname()
        for ip in socket.gethostbyname_ex(hostname)[2]:
            if not ip.startswith("127."):
                ip_list.append(ip)
    except Exception:
        pass
    
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect(("8.8.8.8", 80))
        primary_ip = s.getsockname()[0]
        s.close()
        if primary_ip not in ip_list:
            ip_list.insert(0, primary_ip)
    except Exception:
        pass
    return ip_list

class LogReceiverHandler(BaseHTTPRequestHandler):

    def do_GET(self):
        self.send_response(200)
        self.send_header("Content-Type", "text/html; charset=utf-8")
        self.end_headers()
        html = """
        <html>
        <head><title>OpRoom Log Receiver</title></head>
        <body style="font-family: Arial; padding: 40px; text-align: center;">
            <h1 style="color: #2E7D32;">May chu nhan log phong mo dang hoat dong</h1>
            <p>Trang thai: San sang nhan file tu Tinker Board tai cong 8080.</p>
        </body>
        </html>
        """
        self.wfile.write(html.encode("utf-8"))

    def do_POST(self):
        client_ip = self.client_address[0]
        content_length = int(self.headers.get("Content-Length", 0))

        if content_length <= 0:
            self.send_response(400)
            self.end_headers()
            self.wfile.write(b"No data received.")
            return

        body = self.rfile.read(content_length)

        if not os.path.exists(SAVE_DIR):
            os.makedirs(SAVE_DIR)

        content_type = self.headers.get("Content-Type", "")
        file_bytes = body
        saved_filename = f"OpRoom_Logs_{datetime.now().strftime('%Y%m%d_%H%M%S')}.zip"

        if "multipart/form-data" in content_type:
            try:
                # Tim header cua file zip (PK\x03\x04)
                pk_index = body.find(b"PK\x03\x04")
                if pk_index != -1:
                    boundary = content_type.split("boundary=")[-1].strip().encode()
                    end_index = body.rfind(b"\r\n--" + boundary)
                    if end_index != -1 and end_index > pk_index:
                        file_bytes = body[pk_index:end_index]
                    else:
                        file_bytes = body[pk_index:]
            except Exception as e:
                print(f"[!] Canh bao boc tach multipart: {e}")

        save_path = os.path.join(SAVE_DIR, saved_filename)
        with open(save_path, "wb") as f:
            f.write(file_bytes)

        file_size_kb = len(file_bytes) / 1024.0

        # Tu dong giai nen file zip de xem ngay cac file log
        import zipfile
        extract_folder = os.path.join(SAVE_DIR, os.path.splitext(saved_filename)[0])
        extracted_files = []
        try:
            with zipfile.ZipFile(save_path, "r") as zf:
                zf.extractall(extract_folder)
                extracted_files = zf.namelist()
        except Exception as e:
            pass

        print(f"\n==============================================================")
        print(f"[THANH CONG] Da nhan file log tu Tinker Board (IP: {client_ip})")
        print(f"  - Ten file zip:  {saved_filename}")
        print(f"  - Dung luong:    {file_size_kb:.2f} KB")
        print(f"  - Thu muc luu:   {os.path.abspath(save_path)}")
        if extracted_files:
            print(f"  - Da giai nen vao: {os.path.abspath(extract_folder)}")
            print("  - Danh sach file:")
            for ef in extracted_files:
                tag = ""
                if ef.startswith("sensor_"):
                    tag = " (Nhat ky thong so cam bien - 2 ngay)"
                elif ef.startswith("log_"):
                    tag = " (Nhat ky su kien / thao tac)"
                elif ef.startswith("terminal_"):
                    tag = " (Nhat ky terminal / logcat)"
                print(f"     + {ef}{tag}")
        print(f"==============================================================\n")

        self.send_response(200)
        self.send_header("Content-Type", "application/json")
        self.end_headers()
        self.wfile.write(b'{"status":"ok","message":"File received successfully"}')

    def log_message(self, format, *args):
        pass

def main():
    ips = get_all_ip_addresses()
    print("=" * 68)
    print("  MAY CHU NHAN FILE LOG - BANG DIEU KHIEN PHONG MO SIN HO")
    print("=" * 68)
    print("Cac dia chi IP cua may tinh ban hien tai:")
    for ip in ips:
        if ip.startswith("100."):
            print(f"  -> {ip:18} (Dia chi mang Tailscale)")
        else:
            print(f"  -> {ip:18} (Dia chi mang Wi-Fi / LAN)")
    print("-" * 68)
    print("Huong dan:")
    print("1. Nhap 1 trong cac dia chi IP tren vao man hinh bang dieu khien.")
    print("2. Bam nut 'GUI LOG'.")
    print(f"File log se tu dong duoc luu vao thu muc: {os.path.abspath(SAVE_DIR)}")
    print(f"Dang lang nghe tai cong {PORT}... (Bam Ctrl + C de dung)")
    print("=" * 68 + "\n")

    server = HTTPServer(("0.0.0.0", PORT), LogReceiverHandler)
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print("\nDa dung may chu nhan log.")
        sys.exit(0)

if __name__ == "__main__":
    main()
