package org.walkframework.base.tools.utils;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;

/**
 * 验证码生产工具
 * 
 */
public class ImageMan {
	
	private static final char[] chars = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'x', 'y', 'z' };

	
	/**
	 * 生成验证码
	 * 
	 * @param response
	 * @param width
	 * @param height
	 * @param weight
	 * @return
	 * @throws Exception
	 */
	public static String showValidateImage(HttpServletResponse response, int width, int height) throws Exception {
		return showValidateImage(response, width, height, 1);
	}
	/**
	 * 生成验证码
	 * 
	 * @param response
	 * @param width
	 * @param height
	 * @param weight
	 * @return
	 * @throws Exception
	 */
	public static String showValidateImage(HttpServletResponse response, int width, int height, int weight) throws Exception {
		int charCount = 4;
		int charWidth = width / (charCount + 1);
		int charHeight = height - 8;
		int fontHeight = height - 2;

		StringBuffer str = new StringBuffer();
		Random random = new Random();

		BufferedImage image = new BufferedImage(width, height, 1);
		Graphics graphics = image.getGraphics();

		graphics.setColor(Color.WHITE);
		graphics.fillRect(0, 0, width, height);

		Font font = new Font("Times New Roman", 0, fontHeight);
		graphics.setFont(font);

		graphics.setColor(Color.GRAY);
		graphics.drawRect(0, 0, width - 1, height - 1);

		graphics.setColor(Color.GRAY);
		int lineNum = width * height * weight / 100;
		lineNum = lineNum > 20 ? lineNum : 20;
		for (int i = 0; i < lineNum; ++i) {
			int x = random.nextInt(width);
			int y = random.nextInt(height);
			int padX = random.nextInt(12);
			int padY = random.nextInt(12);
			graphics.drawLine(x, y, x + padX, y + padY);
		}

		for (int i = 0; i < charCount; ++i) {
			String rand = String.valueOf(chars[random.nextInt(chars.length)]);
			int padding = (i == 0) ? 2 : 0;
			int red = random.nextInt(100);
			int green = random.nextInt(100);
			int blue = random.nextInt(100);
			graphics.setColor(new Color(red, green, blue));
			graphics.drawString(rand, (charWidth + 4) * i + padding, charHeight);
			str.append(rand);
		}
		graphics.dispose();

		response.resetBuffer();
		response.setContentType("image/jpeg");

		response.setHeader("Pragma", "No-cache");
		response.setHeader("Cache-Control", "no-cache");
		response.setDateHeader("Expires", 0L);

		response.addHeader("P3P", "CP=\"IDC DSP COR CURa ADMa OUR IND PHY ONL COM STA\"");
		OutputStream out = response.getOutputStream();

		//JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
		//encoder.encode(image);
		ImageIO.write(image, "jpg", out);
		out.close();

		return str.toString();
	}

}