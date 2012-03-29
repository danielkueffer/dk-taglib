package com.danielkueffer.mgnl.dmsimg;

import info.magnolia.cms.beans.runtime.File;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.context.MgnlContext;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.jcr.RepositoryException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

@SuppressWarnings("serial")
public class DmsImg extends TagSupport {
	
	private String uuid;
	private String width;
	private String height;
	private String css;

	public int doStartTag() throws JspException {
		HierarchyManager hm = MgnlContext.getHierarchyManager("dms");
		
		try {
			
			Content con = hm.getContentByUUID(this.getUuid());
			
			// Get last modification date
			Calendar modDate = con.getNodeData("modificationDate").getDate();
			Date modNow = new Date(modDate.getTimeInMillis());
			Long modTime = modNow.getTime();
			
			// Get last resize date
			Calendar dmsImgCreated = con.getNodeData("dmsImgCreated").getDate();
			
			// Get file
			File file = new File(con.getNodeData("document"));
			
			InputStream is = file.getStream();
			BufferedImage bi = null;
			try {
				bi = ImageIO.read(is);
			} 
			catch (IOException e3) {
				e3.printStackTrace();
			}
			
			int w = bi.getWidth();
			int h = bi.getHeight();
			
			int targetWidth, targetHeight;
			
			if (this.width != null && ! this.width.equals("") && (this.height == null || this.height.equals(""))) {
				targetWidth = Integer.parseInt(this.width);
				targetHeight = Math.round(targetWidth * ((float) h / w));
			}
			else if ((this.width == null || this.width.equals("")) && this.height != null && ! this.height.equals("")) {
				targetHeight = Integer.parseInt(this.height);
				targetWidth = Math.round(targetHeight * ((float) w / h));
			}
			else if (this.width != null && ! this.width.equals("") && this.height != null && ! this.height.equals("")) {
				targetWidth = Integer.parseInt(this.width);
				targetHeight = Integer.parseInt(this.height);
			}
			else {
				targetWidth = w;
				targetHeight = h;
			}
			
			String filename = file.getFileName() + "." + file.getExtension();
			
			String realPath = pageContext.getSession().getServletContext().getRealPath("/") + "docroot/";

			String dir = realPath + "dmsimg/" + targetWidth + "x" + targetHeight;

			String newFile = dir + "/" +filename;
			
			boolean createImg = false;
			
			// Check if image was created before and if modification date is newer
			if (dmsImgCreated == null) {
				createImg = true;
			}
			else {
				Date dmsImgCreatedDate = new Date(dmsImgCreated.getTimeInMillis());
				Long dmsImgCreatedTime = dmsImgCreatedDate.getTime();
				
				if (dmsImgCreatedTime < modTime) {
					createImg = true;
				}
			}
			
			// Only write file if it not exists of the file was modified
			if (! new java.io.File(newFile).exists() || createImg) {
				
				java.io.File newDir = new java.io.File(dir);

				if (! newDir.exists()) {
					newDir.mkdirs();
				}
				
				int type = (bi.getTransparency() == Transparency.OPAQUE) ? 
						BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
				
				if (targetWidth > w || targetHeight > h) {
					BufferedImage tmp = new BufferedImage(targetWidth, targetHeight, type);
					Graphics2D g2 = tmp.createGraphics();
					g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
					g2.drawImage(bi, 0, 0, targetWidth, targetHeight, null);
					g2.dispose();

					bi = tmp;
				}
				else {
					do {
						if (w > targetWidth) {
							w/= 2;
							if (w < targetWidth) {
								w = targetWidth;
							}
						}
						
						if (h > targetHeight) {
							h/= 2;
							
							if (h < targetHeight) {
								h = targetHeight;
							}
						}
						
						BufferedImage tmp = new BufferedImage(w, h, type);
						Graphics2D g2 = tmp.createGraphics();
						g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
						g2.drawImage(bi, 0, 0, w, h, null);
						g2.dispose();

						bi = tmp;
						
					}
					while (w != targetWidth || h != targetHeight);
				}
			
				OutputStream os = null;
				
				try {
					os = new FileOutputStream(newFile);
				} 
				catch (FileNotFoundException e1) {
					e1.printStackTrace();
				}
					
				try {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					ImageIO.write(bi, file.getExtension(), baos);
					InputStream isImage = new ByteArrayInputStream(baos.toByteArray());
					
					byte[] buffer = new byte[(int) file.getSize()];
					
					int bytesRead;
					while ((bytesRead = isImage.read(buffer)) != -1) {
						os.write(buffer, 0, bytesRead);
					}
					
					os.close();
					is.close();
					
					con.setNodeData("dmsImgCreated", Calendar.getInstance());
					hm.save();
					
					System.out.println("Image saved to: " + dir);
					
				} 
				catch (IOException e) {
					e.printStackTrace();
				}
			}
			else {
				System.out.println("File exists");
			}
		} 
		catch (RepositoryException e) {
			e.printStackTrace();
		}
		
		try {
			JspWriter out = pageContext.getOut();
			out.println(this.uuid + " saved");
		}
		catch (IOException e) {
			e.printStackTrace();
		} 
		
		return SKIP_BODY;
	}

	public String getWidth() {
		return width;
	}

	public void setWidth(String width) {
		this.width = width;
	}

	public String getHeight() {
		return height;
	}

	public void setHeight(String height) {
		this.height = height;
	}

	public String getCss() {
		return css;
	}

	public void setCss(String css) {
		this.css = css;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
}