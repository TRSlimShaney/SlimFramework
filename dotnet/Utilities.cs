using System;
using System.Threading;
using System.Text.Json;
using System.Text;
using static Framework.Statuses;
using System.Collections.Generic;

namespace Framework
{
    public class FrwSemaphore
    {

        int capacity;
        Semaphore s;
        string name { get; }

        public FrwSemaphore(int capacity, string name)
        {
            this.capacity = capacity;
            this.s = new Semaphore(capacity, capacity);
            this.name = name;
        }

        public void Lock()
        {
            s.WaitOne();
        }

        public void Unlock()
        {
            s.Release();
        }
    }

    public static class Utilities
    {
        static Dictionary<int, String> StatusNames = new Dictionary<int, string>() {
            {STA_NORMAL, "STA_NORMAL"},
            {STA_FAIL, "STA_FAIL"},
            {STA_KEYINUSE, "STA_KEYINUSE"},
            {STA_NOKEY, "STA_NOKEY"},
            {STA_INVPARAMS, "STA_INVPARAMS"}
        };

        public static string GetStatusName(int status)
        {
            if (StatusNames.TryGetValue(status, out string name))
            {
                return name;
            }
            return $"STATUS_NOT_FOUND: {status}";
        }

        public static string ToJson(object obj)
        {
            return JsonSerializer.Serialize(obj);
        }

        public static T FromJson<T>(string json)
        {
            return JsonSerializer.Deserialize<T>(json);
        }

        public static string ExceptionToString(Exception ex)
        {
            return $"Exception: {ex.Message}";
        }

        public static bool SUCCESS(int status)
        {
            return status >= STA_NORMAL;
        }

        public static bool FAILURE(int status)
        {
            return !SUCCESS(status);
        }

        public static bool IsEmptyString(string empty)
        {
            return string.IsNullOrWhiteSpace(empty);
        }

        public static bool IsNotEmptyString(string empty)
        {
            return !IsEmptyString(empty);
        }

        public static bool IsNull(object obj)
        {
            return obj == null;
        }

        public static bool IsNotNull(object obj)
        {
            return !IsNull(obj);
        }

        public static bool IsInt(string integer, out int parsed)
        {
            if (IsEmptyString(integer))
            {
                parsed = 0;
                return false;
            }
            if (int.TryParse(integer, out parsed))
            {
                return true;
            }
            return false;
        }

        public static int ToInt(string integer)
        {
            return int.Parse(integer);
        }

        public static bool IsNullOrZero(int? zero)
        {
            return (IsNull(zero) || zero == 0);
        }

        public static bool IsNotNullOrZero(int? zero)
        {
            return !IsNullOrZero(zero);
        }

        public static bool IsNullOrZero(long? zero)
        {
            long zro = 0;
            return (IsNull(zero) || zero == zro);
        }

        public static bool IsNotNullOrZero(long? zero)
        {
            return !IsNullOrZero(zero);
        }

        public static bool IsNullOrFalse(bool? boolean)
        {
            return (IsNull(boolean) || boolean == false);
        }

        public static bool IsNotNullOrFalse(bool? boolean)
        {
            return !IsNullOrFalse(boolean);
        }

        public static bool IsEmptyList<T>(List<T> list)
        {
            return (IsNull(list) || list.Count <= 0);
        }

        public static bool IsNotEmptyList<T>(List<T> list)
        {
            return !IsEmptyList(list);
        }

        public static bool IsEmptyDict<T1, T2>(Dictionary<T1, T2> dict)
        {
            return (IsNull(dict) || dict.Count <= 0);
        }

        public static bool IsNotEmptyDict<T1, T2>(Dictionary<T1, T2> dict)
        {
            return !IsEmptyDict(dict);
        }

        public static void PrintLine(string msg)
        {
            Console.WriteLine(msg);
        }

        public static byte[] ToBytes(string characters)
        {
            return Encoding.ASCII.GetBytes(characters);
        }

        public static string GetGUID()
        {
            return Guid.NewGuid().ToString();
        }

        public static Dictionary<TOne, TTwo> ToDictionary<TOne, TTwo>(List<List<object>> records)
        {
            var dict = new Dictionary<TOne, TTwo>();
            foreach (var record in records)
            {
                var a = (TTwo)Activator.CreateInstance(typeof(TTwo), new object[] { record });
                dict.Add((TOne)record[0], a);
            }
            return dict;
        }

        public static void Entering(string classname, string routine)
        {
            Logger.Debug(classname, routine, "Entering method");
        }

        public static int GetRandomIntBetween(int InclusiveMin, int ExclusiveMax)
        {
            var rand = new Random();
            return rand.Next(InclusiveMin, ExclusiveMax);
        }

        public static int GetRandomIntBetween(int InclusiveMin, int ExclusiveMax, HashSet<int> Excluding)
        {
            int random;
            do
            {
                random = GetRandomIntBetween(InclusiveMin, ExclusiveMax);
            }
            while (Excluding.Contains(random));
            return random;
        }

        public static bool IsGreaterThan(int a, int b)
        {
            return a > b;
        }

        public static bool IsLessThan(int a, int b)
        {
            return a < b;
        }

        public static bool AreEqual(object a, object b)
        {
            return a == b;
        }

        public static bool IsGreaterThan(string a, string b)
        {
            int result = string.Compare(a, b);
            return result > 0;
        }

        public static bool IsLessThan(string a, string b)
        {
            int result = string.Compare(a, b);
            return result < 0;
        }
    }
}
