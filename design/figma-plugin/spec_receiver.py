# Dev helper: receives the Figma spec dump from the plugin and writes it to the file given as argv[1].
import sys
from http.server import BaseHTTPRequestHandler, HTTPServer
OUT = sys.argv[1]
class H(BaseHTTPRequestHandler):
    def _cors(self):
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Access-Control-Allow-Methods', 'POST, OPTIONS')
        self.send_header('Access-Control-Allow-Headers', '*')
    def do_OPTIONS(self):
        self.send_response(204); self._cors(); self.end_headers()
    def do_POST(self):
        body = self.rfile.read(int(self.headers.get('Content-Length', 0)))
        open(OUT, 'wb').write(body)
        self.send_response(200); self._cors(); self.end_headers(); self.wfile.write(b'ok')
        print('received', len(body), flush=True)
HTTPServer(('127.0.0.1', 8765), H).serve_forever()
