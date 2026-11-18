import http.server
import socketserver
import os

PORT = 8000
DIRECTORY = r"E:\android application\VediRashaka"

class Handler(http.server.SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=DIRECTORY, **kwargs)

if __name__ == "__main__":
    with socketserver.TCPServer(("0.0.0.0", PORT), Handler) as httpd:
        print(f"Serving VediRaksha Web Application at http://localhost:{PORT}")
        httpd.serve_forever()
