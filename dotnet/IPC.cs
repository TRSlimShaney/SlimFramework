using System;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;
using System.Collections.Generic;
using static Framework.Utilities;
using static Framework.Statuses;
using static Framework.Logger;



namespace Framework
{
    public class IncomingRequest
    {
        public TcpClient client { get; }
        public string msg { get; }

        public IncomingRequest(TcpClient client, string msg)
        {
            this.client = client;
            this.msg = msg;
        }
    }

    public class SocketMap
    {
        public string mapping { get; }
        public string IP { get; }
        public int port { get; }

        public SocketMap(string mapping, string IP, int port)
        {
            this.mapping = mapping;
            this.IP = IP;
            this.port = port;
        }
    }
    public class FrwServer
    {
        TcpListener server = null;
        public string name { get; }

        Queue<IncomingRequest> queue;

        public FrwServer(string ip, int port, string name, Queue<IncomingRequest> queue, LoggingLevels level)
        {
            var addr = IPAddress.Parse(ip);
            this.server = new TcpListener(addr, port);
            this.name = $"{name}Server";
            this.queue = queue;
            server.Start();
            StartListener();
        }

        public void StartListener()
        {
            string routine = "StartListener";
            try
            {
                while (true)
                {
                    Debug(name, routine, "Waiting for connection");
                    var client = server.AcceptTcpClient();
                    Debug(name, routine, "Connection accepted");
                    var t = new Thread(new ParameterizedThreadStart(HandleSocket));
                    t.Start(client);
                }
            }
            catch (Exception ex)
            {
                Error(name, routine, ExceptionToString(ex));
                server.Stop();
            }
        }

        void HandleSocket(Object obj)
        {
            string routine = "HandleSocket";
            var client = (TcpClient)obj;
            var stream = client.GetStream();
            var data = new StringBuilder();
            var bytes = new Byte[256];

            try
            {
                while (stream.Read(bytes, 0, bytes.Length) != 0)
                {
                    data.Append(Encoding.ASCII.GetString(bytes));
                }
                Debug(name, routine, $"msg received: {data}");
                queue.Enqueue(new IncomingRequest(client, data.ToString()));
            }
            catch (Exception ex)
            {
                Error(name, routine, ExceptionToString(ex));
            }
        }
    }

    public static class IPC
    {
        const string name = "FrwIPC";
        static Dictionary<string, SocketMap> SocketMapping = new Dictionary<string, SocketMap>();

        public static int AddSocketMapping(string mapping, string ip, int port)
        {
            mapping = mapping.ToLower();
            if (SocketMapping.ContainsKey(mapping))
            {
                return STA_KEYINUSE;
            }
            SocketMapping.Add(mapping, new SocketMap(mapping, ip, port));
            return STA_NORMAL;
        }


        public static void SendResponse(TcpClient client, Object obj)
        {
            string routine = "FrwSendWithRsp";
            try
            {
                var stream = client.GetStream();
                var bytes = ToBytes(ToJson(obj));
                stream.Write(bytes, 0, bytes.Length);
                client.Close();
            }
            catch (Exception ex)
            {
                Error(name, routine, ExceptionToString(ex));
            }
        }

        public static T SendWithResponse<T>(string mapping, Object obj)
        {
            string routine = "FrwSendWithResponse";
            var msg = new StringBuilder();
            try
            {
                var s = SendMessage(mapping, obj);
                var rsp = new Byte[256];
                Debug(name, routine, "Receiving bytes");
                while (s.Receive(rsp) != 0)
                {
                    msg.Append(Encoding.ASCII.GetString(rsp));
                }
                Debug(name, routine, $"Done receiving bytes: {msg}");
                Debug(name, routine, $"Closing socket to {mapping}.");
                s.Close();
            }
            catch (Exception ex)
            {
                Error(name, routine, ExceptionToString(ex));
            }
            return FromJson<T>(msg.ToString());
        }

        public static void SendWithNoResponse(string mapping, Object obj)
        {
            string routine = "FrwSendWithNoResponse";
            try
            {
                var s = SendMessage(mapping, obj);
                Debug(name, routine, $"Closing socket to {mapping}.");
                s.Close();
            }
            catch (Exception ex)
            {
                Error(name, routine, ExceptionToString(ex));
            }
        }

        static Socket SendMessage(string mapping, Object obj)
        {
            string routine = "SendMessage";
            try
            {
                var dictmap = SocketMapping[mapping];
                var ipe = new IPEndPoint(IPAddress.Parse(dictmap.IP), dictmap.port);
                var s = new Socket(ipe.AddressFamily, SocketType.Stream, ProtocolType.Tcp);
                byte[] bytes;

                if (obj.GetType() == typeof(StringBuilder))
                {
                    Debug(name, routine, "obj is a StringBuilder.");
                    bytes = ToBytes(obj.ToString());
                }
                else if (obj.GetType() == typeof(byte[]))
                {
                    Debug(name, routine, "obj is a byte array.");
                    bytes = (byte[]) obj;
                }
                else
                {
                    Debug(name, routine, "obj is an object.");
                    bytes = ToBytes(ToJson(obj));
                }

                Debug(name, routine, $"Connecting to {dictmap.mapping} at IP {dictmap.IP} and Port {dictmap.port}");
                s.Connect(ipe);
                Debug(name, routine, "Connection successful. Sending bytes");
                s.Send(bytes);
                return s;
            }
            catch (Exception ex)
            {
                Error(name, routine, ExceptionToString(ex));
            }
            return null;
        }
    }
}
