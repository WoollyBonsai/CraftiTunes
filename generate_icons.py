import os
import subprocess

icons = {
    "spotify": '<path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm4.6 14.4c-.2.3-.6.4-.9.2-2.5-1.5-5.7-1.9-9.5-1-.4.1-.7-.2-.8-.6-.1-.4.2-.7.6-.8 4.2-1 7.7-.5 10.5 1.2.3.2.4.6.1 1zm1-2.2c-.3.4-.8.5-1.2.3-2.9-1.8-6.9-2.3-10.6-1.3-.4.1-.9-.2-1-.6-.1-.4.2-.9.6-1 4.2-1.1 8.7-.6 12 1.4.4.3.5.8.2 1.2zm.1-2.4c-3.4-2-8.1-2.2-11.8-1.2-.5.1-1-.2-1.2-.7-.1-.5.2-1 .7-1.2 4.3-1.1 9.5-.9 13.4 1.4.5.3.6.9.3 1.4-.3.4-.9.6-1.4.3z"/>',
    "yt_music": '<path d="M10 15l5.19-3L10 9v6m11.56-7.83c.13.47.22 1.1.28 1.9.07.8.1 1.49.1 2.09L22 12c0 2.19-.16 3.8-.44 4.83-.25.9-.83 1.48-1.73 1.73-.47.13-1.33.22-2.65.28-1.3.07-2.49.1-3.59.1L12 19c-4.19 0-6.8-.16-7.83-.44-.9-.25-1.48-.83-1.73-1.73-.13-.47-.22-1.1-.28-1.9-.07-.8-.1-1.49-.1-2.09L2 12c0-2.19.16-3.8.44-4.83.25-.9.83-1.48 1.73-1.73.47-.13 1.33-.22 2.65-.28 1.3-.07 2.49-.1 3.59-.1L12 5c4.19 0 6.8.16 7.83.44.9.25 1.48.83 1.73 1.73z"/>',
    "apple": '<path d="M12 3v10.55c-.59-.34-1.27-.55-2-.55-2.21 0-4 1.79-4 4s1.79 4 4 4 4-1.79 4-4V7h4V3h-6z"/>',
    "prime": '<path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 14.5v-9l6 4.5-6 4.5z"/>',
    "local": '<path d="M10 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2h-8l-2-2z"/>',
    "settings": '<rect x="2" y="2" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2"/><circle cx="12" cy="12" r="7" fill="none" stroke="currentColor" stroke-width="2"/><line x1="12" y1="5" x2="12" y2="19" stroke="currentColor" stroke-width="2"/><line x1="5" y1="12" x2="19" y2="12" stroke="currentColor" stroke-width="2"/>'
}

out_dir = "src/main/resources/assets/craftitunes/textures/gui/icons"
os.makedirs(out_dir, exist_ok=True)

for name, svg_body in icons.items():
    svg_content = f"""<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="64" height="64" fill="white" color="white">{svg_body}</svg>"""
    svg_path = os.path.join(out_dir, f"{name}.svg")
    png_path = os.path.join(out_dir, f"{name}.png")
    
    with open(svg_path, "w") as f:
        f.write(svg_content)
        
    # convert to png
    subprocess.run(["convert", "-background", "none", svg_path, png_path])
    
print("Icons generated successfully!")
