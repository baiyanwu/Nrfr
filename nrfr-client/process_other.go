//go:build !windows

package main

// Fork 变更说明：本文件为 Ackites/Nrfr fork 新增，用于非 Windows 平台进程配置。

import "syscall"

func hiddenWindowSysProcAttr() *syscall.SysProcAttr {
	return nil
}
