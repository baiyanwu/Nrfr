//go:build windows

package main

// Fork 变更说明：本文件为 Ackites/Nrfr fork 新增，用于 Windows 平台隐藏 ADB 控制台窗口。

import "syscall"

func hiddenWindowSysProcAttr() *syscall.SysProcAttr {
	return &syscall.SysProcAttr{
		HideWindow:    true,
		CreationFlags: 0x08000000, // CREATE_NO_WINDOW
	}
}
