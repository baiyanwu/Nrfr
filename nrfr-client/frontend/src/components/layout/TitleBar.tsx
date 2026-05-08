import React, {useState} from 'react';
import logo from '../../assets/images/logo.png';
import {WindowClose, WindowMaximise, WindowMinimise} from '../../../wailsjs/go/main/App';
import {BrowserOpenURL} from '../../../wailsjs/runtime/runtime';

export const TitleBar: React.FC = () => {
    const [showAbout, setShowAbout] = useState(false);

    const openProject = () => {
        BrowserOpenURL('https://github.com/baiyanwu/Nrfr');
    };

    const openUpstream = () => {
        BrowserOpenURL('https://github.com/Ackites/Nrfr');
    };

    return (
        <div className="relative">
            <div className="bg-white/30 backdrop-blur-md h-8 flex items-center px-4"
                 style={{"--wails-draggable": "drag"} as React.CSSProperties}>
                <div className="flex-1 flex items-center space-x-2">
                    <img src={logo} className="w-4 h-4" alt="logo"/>
                    <div className="text-sm font-semibold text-gray-700">Nrfr - 快速启动工具</div>
                </div>
                <div className="flex space-x-2" style={{"--wails-draggable": "no-drag"} as React.CSSProperties}>
                    <button
                        className="text-gray-600 hover:text-gray-800 px-2"
                        onClick={() => setShowAbout(!showAbout)}
                        title="关于"
                        aria-label="打开关于页面"
                    >
                        <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" viewBox="0 0 20 20"
                             fill="currentColor">
                            <path fillRule="evenodd"
                                  d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z"
                                  clipRule="evenodd"/>
                        </svg>
                    </button>
                    <button className="window-control-button bg-yellow-400 hover:bg-yellow-500" title="最小化"
                            aria-label="最小化窗口" onClick={() => WindowMinimise()}></button>
                    <button className="window-control-button bg-green-400 hover:bg-green-500" title="最大化"
                            aria-label="最大化窗口" onClick={() => WindowMaximise()}></button>
                    <button className="window-control-button bg-red-400 hover:bg-red-500" title="关闭"
                            aria-label="关闭窗口" onClick={() => WindowClose()}></button>
                </div>
            </div>

            {showAbout && (
                <div
                    className="fixed right-2 top-10 w-80 bg-white/95 backdrop-blur-md rounded-xl shadow-2xl border border-gray-100 p-4 z-50"
                    style={{"--wails-draggable": "no-drag"} as React.CSSProperties}>
                    <div className="space-y-4">
                        <button
                            onClick={() => setShowAbout(false)}
                            className="absolute right-2 top-2 text-gray-400 hover:text-gray-600 transition-colors"
                            title="关闭"
                            aria-label="关闭关于页面"
                        >
                            <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" fill="none" viewBox="0 0 24 24"
                                 stroke="currentColor">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5}
                                      d="M6 18L18 6M6 6l12 12"/>
                            </svg>
                        </button>

                        <div className="pr-6">
                            <div className="text-sm font-semibold text-gray-800">Nrfr Android 16 适配版</div>
                            <p className="mt-1 text-xs leading-5 text-gray-600">
                                免 Root 的 SIM 卡国家码修改工具，使用 Shizuku 和双 APK helper 适配 Android 16。
                            </p>
                        </div>

                        <div className="space-y-2 rounded-lg bg-blue-50/60 p-3 text-sm">
                            <div className="flex items-center justify-between">
                                <span className="text-gray-600">维护者</span>
                                <span className="font-medium text-gray-800">baiyanwu</span>
                            </div>
                            <button
                                onClick={openProject}
                                className="flex w-full items-center justify-between text-blue-500 transition-colors hover:text-blue-600"
                            >
                                <span>当前项目</span>
                                <span className="font-medium">baiyanwu/Nrfr</span>
                            </button>
                            <button
                                onClick={openUpstream}
                                className="flex w-full items-center justify-between text-blue-500 transition-colors hover:text-blue-600"
                            >
                                <span>上游项目</span>
                                <span className="font-medium">Ackites/Nrfr</span>
                            </button>
                        </div>

                        <p className="text-xs leading-5 text-gray-500">
                            本项目 fork 自 Ackites/Nrfr，基于 Apache-2.0 许可证二次开发。
                        </p>
                    </div>
                </div>
            )}
        </div>
    );
};
