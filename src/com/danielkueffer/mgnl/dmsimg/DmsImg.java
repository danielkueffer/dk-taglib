package com.danielkueffer.mgnl.dmsimg;

import info.magnolia.cms.beans.runtime.File;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.context.MgnlContext;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
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
	private String alt;
	
	private String newFile;
	private String imgPath;

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

			this.newFile = dir + "/" + filename;
			
			this.imgPath = MgnlContext.getContextPath() + "/docroot/dmsimg/" + targetWidth + "x" + targetHeight + "/" + filename;
			
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
			
			// Only write file if it not exists or the file was modified
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
			
				FileImageOutputStream fios = null;
				
				try {
					fios = new FileImageOutputStream(new java.io.File(newFile));
				} 
				catch (IOException e) {
					e.printStackTrace();
				}
				
				Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName(file.getExtension());
				ImageWriter writer = (ImageWriter)iter.next();
				ImageWriteParam iwp = writer.getDefaultWriteParam();
				
				if (file.getExtension().toLowerCase().equals("jpg") || file.getExtension().toLowerCase().equals("jpeg")) {
					iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
					iwp.setCompressionQuality(1);
				}
				
				writer.setOutput(fios);
				IIOImage iioi = new IIOImage(bi, null, null);
				try {
					writer.write(null, iioi, iwp);
				} catch (IOException e) {
					e.printStackTrace();
				}
				writer.dispose();
				
				con.setNodeData("dmsImgCreated", Calendar.getInstance());
				hm.save();
				
				System.out.println("dk-taglib: Image saved to " + this.imgPath);
			}
		} 
		catch (RepositoryException e) {
			e.printStackTrace();
		}
		
		try {
			JspWriter out = pageContext.getOut();
			
			String imgCss = "";
			
			if (this.css != null) {
				imgCss = this.css;
			}
			
			String imgAlt = "";
			
			if (this.alt != null) {
				imgAlt = this.alt;
			}
			
			
			out.println("<img src=\"" + this.imgPath + "\" class=\"" + imgCss + "\" alt=\"" + imgAlt + "\"/>");
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

	public String getAlt() {
		return alt;
	}

	public void setAlt(String alt) {
		this.alt = alt;
	}
}