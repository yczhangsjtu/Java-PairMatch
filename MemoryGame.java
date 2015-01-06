import javax.swing.*;
import javax.imageio.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.Random;
import java.util.ArrayList;
import java.util.Collections;

public class MemoryGame extends JFrame implements ActionListener
{
    JButton[][] buttons = new JButton[8][8];
    JButton start = new JButton("START");
	int tmpI1 = -1, tmpJ1 = -1;
	int tmpI2 = -1, tmpJ2 = -1;
    GridPanel grid = new GridPanel();
    JPanel panel = new JPanel();
    JLabel ltime = new JLabel("00:00:00");
    Timer clock = new Timer(100,this);
	Timer recov = new Timer(1000,this);
    Random gen = new Random(System.currentTimeMillis());
    long startTime = 0;
    boolean started = false;
	int vanished = 0;

    MemoryGame()
    {
        super("Memory Game");

        setLayout(new BorderLayout());
        grid.setPreferredSize(new Dimension(400,400));
        panel.setPreferredSize(new Dimension(110,400));
        grid.setLayout(new GridLayout(8,8));
        panel.setLayout(new GridLayout(10,1));
        clock.setInitialDelay(0);
        clock.setDelay(100);
        clock.setRepeats(true);
		recov.setInitialDelay(1000);
		recov.setRepeats(false);
		int vanished = 0;

        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++)
            {
                buttons[i][j] = new JButton("");
                grid.add(buttons[i][j]);
                buttons[i][j].addActionListener(this);
            }
        start.addActionListener(this);

        panel.add(start);
        panel.add(ltime);

        add(grid, BorderLayout.WEST);
        add(panel, BorderLayout.EAST);

        setResizable(false);
        setSize(520,420);
        setVisible(true);
    }

    public void paint(Graphics g)
    {
        super.paint(g);
    }

    public void actionPerformed(ActionEvent ae)
    {
        if (ae.getSource() == start)
        {
            startTime = System.currentTimeMillis();
            clock.start();
			start.setText("RESTART");

            ArrayList<Integer> ints = new ArrayList<Integer>();
            for (int i = 0; i < 64; i++) ints.add(i/2);
            Collections.shuffle(ints);
            for (int i = 0; i < 8; i++)
                for (int j = 0; j < 8; j++)
                {
                    grid.setNum(i,j,ints.remove(0));
					buttons[i][j].setVisible(true);
                }
            started = true;
			vanished = 0;
        }
        else if (ae.getSource() == clock)
        {
            long t = (System.currentTimeMillis() - startTime) / 1000;
            long second = t % 60;
            t /= 60;
            long minute = t % 60;
            t /= 60;
            long hour = t;
            String time = Long.toString(hour/10) + Long.toString(hour%10) + ":"
                          + Long.toString(minute/10) + Long.toString(minute%10)
                          + ":" + Long.toString(second/10)
                          + Long.toString(second%10);
            ltime.setText(time);
        }
		else if(ae.getSource() == recov)
		{
			buttons[tmpI1][tmpJ1].setVisible(true);
			buttons[tmpI2][tmpJ2].setVisible(true);
			tmpI1 = tmpJ1 = tmpI2 = tmpJ2 = -1;
		}
        else
        {
            if (!started) return;
			for (int i = 0; i < 8; i++)
			{
				for (int j = 0; j < 8; j++)
				{
					if(ae.getSource() != buttons[i][j]) continue;
					if(tmpI1 < 0 || tmpI1 > 7 || tmpJ1 < 0 || tmpJ1 > 7)
					{
						buttons[i][j].setVisible(false);
						tmpI1 = i; tmpJ1 = j;
						return;
					}
					if(tmpI2 < 0 || tmpI2 > 7 || tmpJ2 < 0 || tmpJ2 > 7)
					{
						buttons[i][j].setVisible(false);
						if(grid.getNum(i,j) == grid.getNum(tmpI1,tmpJ1))
						{
							tmpI1 = -1; tmpJ1 = -1;
							vanished ++;
							if(vanished >= 32)
							{
								long t = 
								(System.currentTimeMillis() - startTime)
									/ 1000;
								long second = t % 60;
								t /= 60;
								long minute = t % 60;
								t /= 60;
								long hour = t;
								clock.stop();
								JOptionPane.showMessageDialog
								(null, "You win! Time spent: " +
								Long.toString(hour) + " hours, " +
								Long.toString(minute) + " minutes, " +
								Long.toString(second) + " seconds."
								);
							}
						}
						else
						{
							tmpI2 = i; tmpJ2 = j;
							recov.start();
						}
					}
				}
			}
        }
    }

    public static void main(String args[])
    {
        MemoryGame win = new MemoryGame();
        win.addWindowListener( new WindowAdapter()
        {
            public void windowClosing( WindowEvent e)
            {
                System.exit(0);
            }
        }
                             );
    }
}

class GridPanel extends JPanel
{
    int[][] nums = new int[8][8];
    boolean[][] v = new boolean[8][8];
    BufferedImage[] bi = new BufferedImage[32];
	
    GridPanel()
    {
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++)
                nums[i][j] = 0;
        try
        {
            for (int i = 0; i < 32; i++)
			{
				BufferedImage buf = ImageIO.read
                        (new File("img/"+Integer.toString(i)+".png"));
                bi[i] = createResizedCopy(buf,50,50,true);
			}
        }
        catch (IOException ie)
        {
        }
    }

    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        int w = getWidth() / 8;
        int h = getHeight() / 8;
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++)
            {
                g.drawImage(bi[nums[i][j]],j*w,i*h,Color.white,null);
            }
    }

    public void setNum(int i, int j, int v)
    {
        if (i >= 0 && i < 8 && j >= 0 && j < 8)
            nums[i][j] = v;
    }

    public int getNum(int i, int j)
    {
        if (i >= 0 && i < 8 && j >= 0 && j < 8)
            return nums[i][j];
        else
            return 0;
    }

    BufferedImage createResizedCopy(Image originalImage,
                                    int scaledWidth, int scaledHeight,
                                    boolean preserveAlpha)
    {
        int imageType = preserveAlpha ? 
			BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage scaledBI =
			new BufferedImage(scaledWidth, scaledHeight, imageType);
        Graphics2D g = scaledBI.createGraphics();
        if (preserveAlpha) {
            g.setComposite(AlphaComposite.Src);
        }
        g.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null);
        g.dispose();
        return scaledBI;
    }
}

