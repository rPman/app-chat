package org.luwrain.app.chat.im;

public interface Events {

	/**
	 * ������� ���������� ��� ������������� ������, 
	 * ����� ���� ��������� ��������� ���������� �������� ��� ����������
	 * @param message �������� ������
	 */
	void onError(String message);
	void onWarning(String message);
	/**
	 * ������� ���������� ����� ������ ������� ���� ������������� �����������
	 * @param message �������������� ���������
	 */
	void on2PassAuth(String message);
	/**
	 * ������� ���������� ��� ��������� �������� �����������
	 */
	void onAuthFinish();
	
	/**
	 * ������� ���������� �� ��������� ���������� ������
	 */
	void onSearchResult();
}
