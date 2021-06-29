using System;
using System.Threading;
using System.IO;

namespace Framework
{

    public static class Logger
    {

        static FrwSemaphore s = new FrwSemaphore(1, "FrwLogger");
        public static LoggingLevels GlobalLoggingLevel { get; set; } = LoggingLevels.None;
        const string path = "./mvc.log";

        public static void Error(string classname, string routine, string msg)
        {
            if (GlobalLoggingLevel >= LoggingLevels.Errors)
            {
                Log("ERR", classname, routine, msg);
            }
        }

        public static void Debug(string classname, string routine, string msg)
        {
            if (GlobalLoggingLevel >= LoggingLevels.Debug)
            {
                Log("DBG", classname, routine, msg);
            }

        }

        public static void Info(string classname, string routine, string msg)
        {
            if (GlobalLoggingLevel >= LoggingLevels.Info)
            {
                Log("INF", classname, routine, msg);
            }

        }

        public static void Extra(string classname, string routine, string msg)
        {
            if (GlobalLoggingLevel >= LoggingLevels.Everything)
            {
                Log("EXT", classname, routine, msg);
            }
        }

        static void Log(string prefix, string classname, string routine, string msg)
        {
            s.Lock();
            string line = $"{DateTime.Now.ToString()}::{classname}::{routine}::{prefix}: {msg}\n";
            Console.Write(line);
            using (var file = File.AppendText(path))
            {
                file.Write(line);
            }
            s.Unlock();
        }
    }
}