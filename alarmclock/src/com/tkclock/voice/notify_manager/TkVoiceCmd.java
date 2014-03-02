package com.tkclock.voice.notify_manager;

import java.util.HashMap;

import android.util.Log;
import android.util.SparseArray;
import static com.tkclock.onalarm.TkAlarmNotify.COMMAND_NAME_STOP;
import static com.tkclock.onalarm.TkAlarmNotify.COMMAND_NAME_NEXT;
import static com.tkclock.onalarm.TkAlarmNotify.COMMAND_NAME_BACK;
import static com.tkclock.onalarm.TkAlarmNotify.COMMAND_NAME_REPEAT;

public class TkVoiceCmd {
	private static String TAG = "VoiceCommand";
	public static String COMMAND_OK_STR = "ok";
	public static String COMMAND_UNSUPPORTED_STR = "unsupport";
	
	// Control command
	public static int COMMAND_STOP 		= 1;
	
	// Navigation command
	public static int COMMAND_REPEAT 	= 2;
	public static int COMMAND_NEXT 		= 3;
	public static int COMMAND_BACK 		= 4;
	
	// Option command
	public static int COMMAND_FB 		= 5;
	public static int COMMAND_MAIL 		= 6;
	public static int COMMAND_NEWS 		= 7;
	public static int COMMAND_WEATHER 	= 8;
	
	private SparseArray<BaseCmd> m_command_list;
	
	public TkVoiceCmd(TkNotificationSpeaker speaker) {
		BaseCmd cmd = null;
		
		m_command_list = new SparseArray<BaseCmd>();
		
		cmd = new StopCmd(speaker);
		m_command_list.put(cmd.getCmdId(), cmd);
		cmd = new RepeatCmd(speaker);
		m_command_list.put(cmd.getCmdId(), cmd);
		cmd = new StopCmd(speaker);
		m_command_list.put(cmd.getCmdId(), cmd);
		cmd = new NextCmd(speaker);
		m_command_list.put(cmd.getCmdId(), cmd);
		cmd = new BackCmd(speaker);
		m_command_list.put(cmd.getCmdId(), cmd);
	}
	
	public String processCommand(String command) {
		BaseCmd commandCmd = null;
		int cmd_result = BaseCmd.COMMAND_OK;
		String result = COMMAND_OK_STR;
		
		for(int i = 0; i <= m_command_list.size(); i++) {	
			commandCmd = m_command_list.get(Integer.valueOf(i));
			if(commandCmd == null) {
				continue;
			}
			
			cmd_result = commandCmd.checkCommand(command);
			if(cmd_result == BaseCmd.COMMAND_UNSUPPORTED) {
				continue;
			} else if(cmd_result == BaseCmd.COMMAND_SUGGESTION) {
				result = commandCmd.getCmdName();
				return result;
			} else {
				Log.d(TAG, "processing command: " + command);
				cmd_result = commandCmd.process();
				break;
			}
		}
		
		return result;
	}
	
	
	
	
	
	
	
	
	
	
	// Implement internal command
	private static abstract class BaseCmd {
		public static int COMMAND_OK = 1;
		public static int COMMAND_SUGGESTION = 2;
		public static int COMMAND_UNSUPPORTED = 3;
		int m_cmd_id;
		String m_cmd_name;
		String m_cmd_similar;
		TkNotificationSpeaker m_speaker;
		
		public BaseCmd(TkNotificationSpeaker speaker) {
			m_cmd_name = "";
			m_cmd_similar = "";
			m_cmd_id = 0;
			
			m_speaker = speaker;
		}
		
		public abstract int process();
		
		public Integer getCmdId() {
			return Integer.valueOf(m_cmd_id);
		}
		
		public String getCmdName() {
			return m_cmd_name;
		}
		
		public int checkCommand(String command) {
			int result = COMMAND_UNSUPPORTED;
			
			if(m_cmd_name.equals(command)) {
				result = COMMAND_OK;
			} else if(m_cmd_similar.contains(command)) {
				result = COMMAND_SUGGESTION; 
			}
			
			return result;
		}
	}
	
	private static class StopCmd extends BaseCmd {

		public StopCmd(TkNotificationSpeaker speaker) {
			super(speaker);
			
			m_cmd_id = COMMAND_STOP;
			m_cmd_name = COMMAND_NAME_STOP;
			m_cmd_similar = "pause;terminate";
		}

		@Override
		public int process() {
			// TODO Auto-generated method stub
			m_speaker.stop();
			return 0;
		}
	}
	
	private static class RepeatCmd extends BaseCmd {

		public RepeatCmd(TkNotificationSpeaker speaker) {
			super(speaker);
			
			m_cmd_id = COMMAND_REPEAT;
			m_cmd_name = COMMAND_NAME_REPEAT;
		}

		@Override
		public int process() {
			m_speaker.repeat();
			return 0;
		}
	}
	
	private static class NextCmd extends BaseCmd {

		public NextCmd(TkNotificationSpeaker speaker) {
			super(speaker);
			
			m_cmd_id = COMMAND_NEXT;
			m_cmd_name = COMMAND_NAME_NEXT;
		}

		@Override
		public int process() {
			m_speaker.next();
			return 0;
		}
	}
	
	private static class BackCmd extends BaseCmd {

		public BackCmd(TkNotificationSpeaker speaker) {
			super(speaker);
			
			m_cmd_id = COMMAND_BACK;
			m_cmd_name = COMMAND_NAME_BACK;
		}

		@Override
		public int process() {
			m_speaker.back();
			return 0;
		}
	}

}
