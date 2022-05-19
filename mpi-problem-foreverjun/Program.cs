using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text.RegularExpressions;


namespace mpi_problem
{
    class Program
    {
        static void Main(string[] args)
        {
            if (args.Count() != 1)
            {
                throw new ArgumentException("The program takes one argument. File path.");
            }
            
            var listOfWords = ExportDadaFromFile(args[0]);

            List<KeyValuePair<string, List<int>>> resultsAllProcs;
            List<KeyValuePair<string, List<int>>> resultData;
            var sortedUnitedDict = new Dictionary<String, List<int>>();

            MPI.Environment.Run(ref args, communicator =>
            {
                int [] tasksSizes = CalculateTasksSizes(communicator.Size,listOfWords.Count);
            
                int startOfBlock = 0;
                for (int i = 0; i < communicator.Rank; i++) startOfBlock += tasksSizes[i];
                var endOfBlock = startOfBlock + tasksSizes[communicator.Rank];
                
                var dict = new Dictionary<String, List<int>>();
                for (int i = startOfBlock; i < endOfBlock; i++)
                {
                    if (!dict.ContainsKey(listOfWords[i])) dict.Add(listOfWords[i], new List<int> {1});
                    else dict[listOfWords[i]].Add(1);
                }
                
                resultsAllProcs = communicator.Reduce(dict.ToList(), 
                    (firstPart, secondPart) => firstPart.Concat(secondPart).ToList(), 0);

                if (communicator.Rank == 0)
                {
                    if (resultsAllProcs != null)
                    {
                        foreach (var (key, value) in resultsAllProcs)
                        {
                            if (!sortedUnitedDict.ContainsKey(key)) sortedUnitedDict.Add(key, value);
                            else sortedUnitedDict[key] = sortedUnitedDict[key].Concat(value).ToList();
                        }
                    }
                    tasksSizes = CalculateTasksSizes(communicator.Size,sortedUnitedDict.Count);
                }
                
                communicator.Broadcast(ref tasksSizes, 0);
                communicator.Broadcast(ref sortedUnitedDict, 0);
                
                startOfBlock = 0;
                for (int i = 0; i < communicator.Rank; i++) startOfBlock += tasksSizes[i];
                endOfBlock = startOfBlock + tasksSizes[communicator.Rank];

                var buff = new List<KeyValuePair<string, List<int>>>();
                for (int i = startOfBlock; i < endOfBlock; i++)
                {
                    int sum = 0;
                    foreach (var value in sortedUnitedDict[sortedUnitedDict.ElementAt(i).Key])
                    {
                        sum += value;
                    }
                    sortedUnitedDict[sortedUnitedDict.ElementAt(i).Key].Clear();
                    sortedUnitedDict[sortedUnitedDict.ElementAt(i).Key].Add(sum);
                    buff.Add(new KeyValuePair<string, List<int>>(sortedUnitedDict.ElementAt(i).Key, sortedUnitedDict.ElementAt(i).Value));
                }
                resultData = communicator.Reduce(buff.ToList(),
                    (firstPart, secondPart) => firstPart.Concat(secondPart).ToList(), 0);

                if (communicator.Rank == 0)
                    foreach (var pair in resultData)
                        Console.WriteLine($"{pair.Key} {pair.Value[0]}");
            });
        }
        private static int[] CalculateTasksSizes (int procNum, int wordsNum)
        {
            if (wordsNum == 0) return Array.Empty<int>();
            int tasksOnOneProc = wordsNum/procNum;
            int remainingWork = wordsNum%procNum;
            int[] jobSizeArr = new int[procNum];
            for (int i = 0; i < procNum; i++) jobSizeArr[i] += tasksOnOneProc;
            for (int i = 0; i < remainingWork; i++) jobSizeArr[i]++;
            return jobSizeArr;
        }

        private static List<String> ExportDadaFromFile (String filePath)
        {
            if (!File.Exists(filePath))
            {
                throw new FileNotFoundException("This file was not found.");
            }
            var listOfWords = new List<string>();
            var counter = 1;
            StreamReader stream = new StreamReader(filePath);
            String str = stream.ReadLine();
            while (str != null)
            {
                String[] tempStr = str.Split(new []{' ', '\n'}, StringSplitOptions.RemoveEmptyEntries);
                foreach (var line in tempStr)
                {
                    if (Regex.IsMatch(line, @"^[a-zA-Z]+$"))
                    {
                        listOfWords.Add(line);
                    }
                    else
                    {
                        throw new ArgumentException("The file must contain only Latin letters, line breaks and spaces." +
                                                    $" An invalid character in the line number {counter}");
                    }
                }

                counter++;
                str = stream.ReadLine();
            }
            stream.Close();
            return listOfWords;
        }
    }
}
