package electron.gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class FreezeScreenImage extends JPanel {

	private BufferedImage image;

	public FreezeScreenImage(BufferedImage image) {
		this.image = image;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (image != null) {
			g.drawImage(image, 0, 0, this);
		}
	}

	@Override
	public Dimension getPreferredSize() {
		if (image != null) {
			return new Dimension(image.getWidth(), image.getHeight());
		}
		return super.getPreferredSize();
	}

	/**
	 * Creates preview window
	 * 
	 * @param image - image to paint
	 */
	public static void paint(BufferedImage image) {
		JFrame frame = new JFrame("Freeze Image");
		FreezeScreenImage displayImage = new FreezeScreenImage(image);
		frame.add(displayImage);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		frame.setVisible(true);
	}
}