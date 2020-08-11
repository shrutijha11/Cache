
import java.io.*;
import java.lang.Math;
import java.util.*;

/*This code includes the level 2 cache implementation(bonus task) along with the level 1 cache implementation(end-sem assignment). */
// Both end-sem assignment and bonus task are implemented in the program below://

class shruti_2019274_FinalAssignment {
	
	public static int[] mainmemory=new int[65536]; //array to implement main memory

	public static HashMap<String,String> tagmap=new HashMap<String,String>(); //hashmap for direct mapping L1
	public static int[] cachearr; //array for direct mapping
	public static HashMap<String,String> tagmap2=new HashMap<String,String>(); //hashmap for fully associative mapping L1
	public static int[] cachearr2; //array for fully associative mapping
	public static HashMap<String,String> tagmap3=new HashMap<String,String>(); //hashmap for k-way set associative mapping L1
	public static int[] cachearr3; //array for k-way set associative mapping
	
	public static HashMap<String,String> cache_L2=new HashMap<String,String>(); //hashmap for level-2 of cache
	public static int[] cachearr_L2; //array for level-2 of cache

	public static void main(String[] args)
	{
		
		for(int i=0;i<65536;i++)
			mainmemory[i]=i; //storing random data in main memory
		
		Scanner in=new Scanner(System.in);
		System.out.println("Enter the block size B: "); //taking inputs 
		int b=in.nextInt();
		System.out.println("Enter the number of cache lines CL: ");
		int cl=in.nextInt();
		
		int size_c=cl*b;
		
		System.out.println("Enter the value of k for k-way set associative mapping: ");
		int k=in.nextInt();
		
		errorChecking1(b,cl,k);

		System.out.println("Enter number of queries Q: ");
		int q=in.nextInt();

		cachearr=new int[size_c];
		cachearr2=new int[size_c];
		cachearr3=new int[size_c];
		
		int offset_bits=(int)(Math.log(b)/Math.log(2)); //number of bits to represent block offset
		int cacheline_bits=(int)(Math.log(cl)/Math.log(2)); //number of bits to represent cache line number
		
		int tag_bits_dm=16-offset_bits-cacheline_bits; //number of bits to represent tag in direct mapping
		int tag_bits_fam=16-offset_bits; //number of bits to represent tag in fully-associative mapping
		
		int setno_bits; //number of bits to represent set number
		int noofsets=(int)(cl/k); //no of sets in k-way set associative mapping
		if(noofsets==1)
			setno_bits=1;
		else
			setno_bits=(int)(Math.log(noofsets)/Math.log(2));
		
		int tag_bits_kwam=16-offset_bits-setno_bits; //number of bits to represent tag in k-way set associative mapping

		directmapping_ini(b,cl,offset_bits, cacheline_bits,tag_bits_dm); //initialising hashmaps and arrays for each mapping 
		associativemapping_ini(b,cl,offset_bits,cacheline_bits,tag_bits_fam);
		kwayassociativemapping_ini(b,cl,k,noofsets,offset_bits,setno_bits,cacheline_bits,tag_bits_kwam);
		level2_cache_ini(b,cl);
		
		for(int z=0; z<q;z++){
		System.out.println("Enter the memory address of the word to be searched in the 2-level cache: "); //MAR input
		int address=in.nextInt();
		errorChecking2(address);
		String mar=func(address);  
		
		System.out.println("OUTPUT for query "+(z+1)+":");
		System.out.println("The main memory can store upto 65536 (2^16) words. It has total "+65536/b+" blocks.");
		System.out.println("Entered memory address "+address+" is stored in block number "+Integer.parseInt(mar.substring(0,tag_bits_dm+cacheline_bits),2)+" in the main memory.");
		//System.out.println("");
		System.out.println("Used 3 different mapping approaches to search for the memory address " +address+" in the 2-LEVEL CACHE: ");
		System.out.println("");
		System.out.println("FIRST searching for the address in CACHE LEVEL-1 (L1) of size "+size_c+" words:");
		System.out.println("(1) DIRECT MAPPING: ");
		print_cache_dm();
		checktag(mar,b,cl,offset_bits, cacheline_bits, tag_bits_dm);
		System.out.println("");
		System.out.println("(2) FULLY ASSOCIATIVE MAPPING: ");
		System.out.println("CACHE L1 CONTENTS IN FULLY ASSOCIATIVE MAPPING: ");
		print_cache_fam();
		checktag2(mar,b,cl,offset_bits,cacheline_bits,tag_bits_fam);
		System.out.println("");
		if(k==cl) {
			System.out.println("(3) "+k+"-WAY SET ASSOCIATIVE MAPPING: ");
			System.out.println("Total number of sets in cache L1= "+1);
			System.out.println("CACHE L1 CONTENTS IN k-WAY SET ASSOCIATIVE MAPPING: ");
			print_cache_fam();
			checktag2(mar,b,cl,offset_bits,cacheline_bits,tag_bits_fam);
			System.out.println("");
		}
		
		else {
			System.out.println("(3) "+k+"-WAY SET ASSOCIATIVE MAPPING: ");
			System.out.println("Total number of sets in cache L1= "+noofsets);
			print_cache_kwam(k,setno_bits);
			checktag3(mar,noofsets,k,b,cl,offset_bits,cacheline_bits,tag_bits_kwam,setno_bits);
			System.out.println("");		
		}
		
		
	}
	in.close();

}
	
	public static void errorChecking1(int b, int cl, int k) //function to check for possible errors in input
	{
		
		
		if((b*cl)>65536)
		{
			System.out.println("LOGICAL ERROR: Cache size greater than main memory size. Re-enter smaller value for B and CL and try again.");
			System.exit(0);
		}
		
		if(k>cl)
		{
			System.out.println("ERROR: The value of k must be less than or equal to the number of cache lines CL. Re-enter value of k and try again.");
			System.exit(0);
		}

		if((cl%2!=0)|| (b%2!=0) || (k%2!=0))
		{
			System.out.println("ERROR: All inputs except the address must be a power of 2. Re-enter parameter and try again.");
			System.exit(0);
		}
		
	}

	public static void errorChecking2(int address) //function to check if entered address is in range
	{
		if(address>65535 || address<0)
		{
			System.out.println("ERROR: Main memory can only store 2^16 words with address ranging from 0 to 2^16-1(i.e. 65535). Re-enter a valid address and try again.");
			System.exit(0);
		}

	}
	
	
	
	
	public static String func(int n) //function to convert MAR to its 16-bit equivalent
	{
		String s=Integer.toBinaryString(n);
		String binarymar=String.format("%16s",s);
		binarymar=binarymar.replace(' ', '0');
	
		return binarymar;
		
	}
	
	public static void print_cache_dm() //function to print cache contents in direct mapping
	{
		System.out.println("CACHE L1 CONTENTS IN DIRECT MAPPING: ");
		tagmap.entrySet().forEach(entry->{
		
			System.out.println("Block number "+ Integer.parseInt(entry.getValue()+entry.getKey(),2)+" is loaded in cache L1 line number "+ Integer.parseInt(entry.getKey(),2)+".");
		
		});
		//System.out.println("");
	}
	
	public static void directmapping_ini(int b, int cl, int offset_bits, int cacheline_bits, int tag_bits_dm) //pre-loading cache L1 for direct mapping case 
	{
		
		for(int i=0; i<cl;i++)
		{	
			String s=Integer.toBinaryString(i);
			
			String line_i=String.format("%"+Integer.toString(cacheline_bits)+"s",s);
			line_i=line_i.replace(' ', '0');
			
			String tag_i_dm=String.format("%"+Integer.toString(tag_bits_dm)+"s",s);
			tag_i_dm=tag_i_dm.replace(' ', '0');
			
			tagmap.put(line_i,tag_i_dm);
			
		}
		
		 tagmap.entrySet().forEach(entry->{
			 for(int j=0;j<b;j++)
			 {
				 	String smap=Integer.toBinaryString(j);
					String offset_i=String.format("%"+Integer.toString(offset_bits)+"s",smap);
					offset_i=offset_i.replace(' ', '0');
				 
					cachearr[Integer.parseInt(entry.getKey()+offset_i,2)]=mainmemory[Integer.parseInt(entry.getValue()+entry.getKey()+offset_i,2)];
			
			 }
	        });
		
	}
	
	
	public static void checktag(String mar, int b, int cl, int offset_bits, int cacheline_bits, int tag_bits_dm) //function to search for MAR in L1 in direct mapping case
	{
		
		String lineno=mar.substring(tag_bits_dm,tag_bits_dm+cacheline_bits); //line number to which MAR can belong to
		System.out.println("Searching for the address in cache L1 line number "+Integer.parseInt(lineno,2)+":-");
		String tag= tagmap.get(lineno); //getting tag of the line number from hashmap
		
		String tagmar= mar.substring(0,tag_bits_dm); //getting tag of MAR
		
		if(tag.equals(tagmar))
		{
			System.out.println("****CACHE L1 HIT****");
			System.out.println("Address found at line number "+Integer.parseInt(lineno,2)+" in cache L1.");
			int index= Integer.parseInt(mar.substring(tag_bits_dm),2);
			
			System.out.print("Data stored at entered memory address:  ");
			System.out.println(cachearr[index]);
			return;
		}
		
		else
		{
			System.out.println("****CACHE L1 MISS****");
			
			String blockincache=tag+lineno;
			String blockreplace=tagmar+lineno;
			
			cache_L2_print(b,cl); //printing L2 contents
			level2_cache_search(mar,b,cl); //function to search for MAR in L2
			System.out.println("Because of cache MISS in L1, block number "+Integer.parseInt(blockincache,2)+" replaced with block number "+Integer.parseInt(blockreplace,2)+" in line number "+Integer.parseInt(lineno,2)+" in cache L1.");
			
			tagmap.remove(lineno);
			tagmap.put(lineno,tagmar); //replacing the tag in hashmap
			
			String blockno=mar.substring(0,tag_bits_dm+cacheline_bits);
			
			int [] indicesm=new int[b];
			int [] indicesc=new int[b];
			for(int i=0; i<b;i++)
			{
				String combi=String.format("%"+Integer.toString(offset_bits)+"s",Integer.toBinaryString(i));
				combi=combi.replace(' ','0');
				
				String indexm=blockno+combi;
				indicesm[i]=Integer.parseInt(indexm,2);
				
				String indexc=lineno+combi;
				indicesc[i]=Integer.parseInt(indexc,2);
				
			}
			
			for(int j=0;j<b;j++)
			{
				cachearr[indicesc[j]]=mainmemory[indicesm[j]]; //copying data into cache array
			}
			
			

		}
			
	}
	
	
	public static void print_cache_fam() //function to print cache contents in fully associative mapping
	{
		
		tagmap2.entrySet().forEach(entry->{
		
			System.out.println("Block number "+ Integer.parseInt(entry.getKey(),2)+" is loaded in cache L1 line number "+ Integer.parseInt(entry.getValue(),2)+".");
		
		});
		//System.out.println("");
	}
	
	public static void associativemapping_ini(int b, int cl, int offset_bits, int cacheline_bits, int tag_bits_fam) //pre-loading cache L1 for fully associative mapping case
	{
		
		for(int i=0; i<cl;i++)
		{	
			String s=Integer.toBinaryString(i);
			
			String line_i=String.format("%"+Integer.toString(cacheline_bits)+"s",s);
			line_i=line_i.replace(' ', '0');
			
			String tag_i_dm=String.format("%"+Integer.toString(tag_bits_fam)+"s",s);
			tag_i_dm=tag_i_dm.replace(' ', '0');
			
			tagmap2.put(tag_i_dm, line_i);
			
		}
		
		
		tagmap2.entrySet().forEach(entry->{
			for(int j=0;j<b;j++) {
				
				String smap=Integer.toBinaryString(j);
				String offset_i=String.format("%"+Integer.toString(offset_bits)+"s",smap);
				offset_i=offset_i.replace(' ', '0');
				
				cachearr2[Integer.parseInt(entry.getValue()+offset_i,2)]=mainmemory[Integer.parseInt(entry.getKey()+offset_i,2)];
			
				}
	        });
		
		
	}
	
	
	public static void checktag2(String mar, int b, int cl, int offset_bits, int cacheline_bits, int tag_bits_fam) //function to search for MAR in L1 in fully associative mapping case
	{
		System.out.println("Searching for the address in all cache lines:-");
		String blockno=mar.substring(0,tag_bits_fam); //getting block number of MAR
		
		if(tagmap2.containsKey(blockno))
		{
			System.out.println("****CACHE L1 HIT****");
			String offset=mar.substring(tag_bits_fam);
			System.out.println("Address found at line number "+Integer.parseInt(tagmap2.get(blockno),2)+" in cache L1.");
			int index=Integer.parseInt(tagmap2.get(blockno)+offset,2);
			System.out.print("Data stored at entered memory address: ");
			System.out.println(cachearr2[index]);
			
			
		}
		else
		{
			System.out.println("****CACHE L1 MISS****");
			String lineno=mar.substring(tag_bits_fam-cacheline_bits,tag_bits_fam);
			String blockinmap="";
			
			for(String key:tagmap2.keySet())
			{
				if(lineno.equals(tagmap2.get(key)))
				{	
					blockinmap=key;
					break;
			}
			}
			
			
			cache_L2_print(b,cl);
			level2_cache_search(mar,b,cl); //calling search in L2
			System.out.println("Because of cache MISS in L1, block number "+Integer.parseInt(blockinmap,2)+" replaced with block number "+Integer.parseInt(blockno,2)+" in line number "+Integer.parseInt(lineno,2)+" in cache L1.");
			
			tagmap2.remove(blockinmap);
			tagmap2.put(blockno,lineno); //replacing block in cache hashmap
			
			int [] indicesm=new int[b];
			int [] indicesc=new int[b];
			for(int i=0; i<b;i++)
			{
				String combi=String.format("%"+Integer.toString(offset_bits)+"s",Integer.toBinaryString(i));
				combi=combi.replace(' ','0');
				
				String indexm=blockno+combi;
				indicesm[i]=Integer.parseInt(indexm,2);
				
				String indexc=lineno+combi;
				indicesc[i]=Integer.parseInt(indexc,2);
				
			}
			
			for(int j=0;j<b;j++)
			{
				cachearr2[indicesc[j]]=mainmemory[indicesm[j]]; //copying data in cache array
			}
			
		}
		
		
	}
	
	
	public static void print_cache_kwam(int k, int setno_bits) //function to print L1 contents in k-way set associative mapping
	{		
			System.out.println("CACHE L1 CONTENTS IN k-WAY SET ASSOCIATIVE MAPPING: ");
			tagmap3.entrySet().forEach(entry->{
			int s=(int)(Integer.parseInt(entry.getKey(),2)/k);
			String smap=Integer.toBinaryString(s);
			String setstring=String.format("%"+Integer.toString(setno_bits)+"s",smap);
			setstring=setstring.replace(' ','0');
			System.out.println("Block number "+ Integer.parseInt(entry.getValue()+setstring,2)+" is loaded in cache L1 line number "+ Integer.parseInt(entry.getKey(),2)+".");
		
		});
		//System.out.println("");
	}
	
	
	public static void kwayassociativemapping_ini(int b, int cl,int k,int noofsets, int offset_bits, int setno_bits, int cacheline_bits, int tag_kwam) //pre-loading cache L1 for k-way set associative case
	{
		
		
		for(int i=0; i<cl;i++)
		{	
			String s=Integer.toBinaryString(i);
			
			String line_i=String.format("%"+Integer.toString(cacheline_bits)+"s",s);
			line_i=line_i.replace(' ', '0');
			
			String tag_i_dm=String.format("%"+Integer.toString(tag_kwam)+"s",s);
			tag_i_dm=tag_i_dm.replace(' ', '0');
			
			tagmap3.put(line_i,tag_i_dm);
			
		}
		
		
		 tagmap3.entrySet().forEach(entry->{
			 
			 	int s=(int)(Integer.parseInt(entry.getKey(),2)/k);
				String smap2=Integer.toBinaryString(s);
				String setstring=String.format("%"+Integer.toString(setno_bits)+"s",smap2);
				setstring=setstring.replace(' ','0');
				
				for(int j=0; j<b;j++) {
				
				
				String smap=Integer.toBinaryString(j);
				String offset_i=String.format("%"+Integer.toString(offset_bits)+"s",smap);
				offset_i=offset_i.replace(' ', '0');
				
				
				cachearr3[Integer.parseInt(entry.getKey()+offset_i,2)]=mainmemory[Integer.parseInt(entry.getValue()+setstring+offset_i,2)];
			
			
			 }
	        });
		
	}
	
	
	
	public static void checktag3(String mar,int noofsets, int k, int b, int cl, int offset_bits, int cacheline_bits, int tag_bits_kwam, int setno_bits) //function to search for MAR in L1 in k-way set associative mapping
	{	

		String set=mar.substring(tag_bits_kwam,tag_bits_kwam+setno_bits); //getting set number of MAR
		int setno=Integer.parseInt(set,2);
		System.out.println("Searching for the address in cache L1 set number "+setno+":-");
		String tagmar=mar.substring(0,tag_bits_kwam);
		String offset=mar.substring(tag_bits_kwam+setno_bits);
		
		boolean flag=false;
		String found="";
		
		for(int i=0; i<cl;i++)
		{
		int ctest=(int)i/k; //checking if a cache line belongs to the set to which MAR belongs to.
		if(ctest==setno)
		{
			
			String s=Integer.toBinaryString(i);
			
			String line_i=String.format("%"+Integer.toString(cacheline_bits)+"s",s);
			line_i=line_i.replace(' ', '0');
			
			if(tagmap3.get(line_i).equals(tagmar))
			{
				
				flag=true;
				System.out.println("****CACHE L1 HIT****");
				found=line_i;
				System.out.println("Address found at line number "+Integer.parseInt(found,2)+", set number "+(Integer.parseInt(found,2)/k)+" in cache L1.");
				int index=Integer.parseInt(found+offset,2);
				System.out.print("Data stored at entered memory address: ");
				System.out.println(cachearr3[index]);
				return;
				
			}
			
			
		}
			
	}
	
	if(flag==false)
	{
		System.out.println("****CACHE L1 MISS****");
		int clno=0;
		
		for(int f=0;f<cl;f++) //getting the first cache line number in the set to which MAR belongs to
		{	
			int test=(int)f/k;
			
			if(test==setno)
			{
				clno=f;
				break;
				
			}
		}
		
		String s2=Integer.toBinaryString(clno);
		
		String line_i2=String.format("%"+Integer.toString(cacheline_bits)+"s",s2);
		line_i2=line_i2.replace(' ', '0');
		
		cache_L2_print(b,cl);
		level2_cache_search(mar,b,cl); //call to search in L2
		System.out.println("Because of cache MISS in L1, block number "+Integer.parseInt(tagmap3.get(line_i2)+set,2)+" replaced with block number "+Integer.parseInt(tagmar+set, 2)+" in cache L1 line number "+Integer.parseInt(line_i2,2)+", set number "+(Integer.parseInt(line_i2,2)/k)+".");
		
		tagmap3.remove(line_i2);
		tagmap3.put(line_i2,tagmar); //replacing block in cache hashmap
		
		int [] indicesm=new int[b];
		int [] indicesc=new int[b];
		for(int i=0; i<b;i++)
		{
			String combi=String.format("%"+Integer.toString(offset_bits)+"s",Integer.toBinaryString(i));
			combi=combi.replace(' ','0');
			
			String indexm=tagmar+set+combi;
			indicesm[i]=Integer.parseInt(indexm,2);
			
			String indexc=line_i2+combi;
			indicesc[i]=Integer.parseInt(indexc,2);
			
		}
		
		for(int j=0;j<b;j++)
		{
			cachearr3[indicesc[j]]=mainmemory[indicesm[j]]; //copying data into cache array
		}
		
		
	
	
		}
	
	}
	
	
	public static void level2_cache_ini(int b, int cl) //function to initialise cache L2 hashmap and array
	{
		int size=cl*b*2;
		cachearr_L2=new int[size];
		int offset_bits=(int)(Math.log(b)/Math.log(2));
		int block_bits=16-offset_bits;
		int cacheline2_bits=(int)(Math.log(cl*2)/Math.log(2));

		for(int i=0,j=cl; i<cl*2 ;i++,j++)
		{	
			String s1=Integer.toBinaryString(i);
			String line_i2=String.format("%"+Integer.toString(cacheline2_bits)+"s",s1);
			line_i2=line_i2.replace(' ', '0');
			
			String s2=Integer.toBinaryString(j);
			String blockno=String.format("%"+Integer.toString(block_bits)+"s",s2);
			blockno=blockno.replace(' ', '0');
			
			cache_L2.put(blockno,line_i2);
			
		}
		
		 cache_L2.entrySet().forEach(entry->{
			 for(int x=0;x<b;x++)
			 {
				 	String smap=Integer.toBinaryString(x);
					String offset_i=String.format("%"+Integer.toString(offset_bits)+"s",smap);
					offset_i=offset_i.replace(' ', '0');
				 
					cachearr_L2[Integer.parseInt(entry.getValue()+offset_i,2)]=mainmemory[Integer.parseInt(entry.getKey()+offset_i,2)];
			
			 }
	        });


	}

	public static void cache_L2_print(int b,int cl) //function to print L2 contents
	{
		System.out.println("");
		System.out.println("NOW searching for the address in CACHE LEVEL-2 (L2) of size "+(cl*b*2)+" words: ");
		System.out.println("CACHE L2 CONTENTS: ");
		cache_L2.entrySet().forEach(entry->{
		
			System.out.println("Block number "+ Integer.parseInt(entry.getKey(),2)+" is loaded in cache L2 line number "+ Integer.parseInt(entry.getValue(),2)+".");
		
		});
	}

	public static void level2_cache_search(String mar, int b, int cl) //function to search for MAR in L2 in case of a cache miss in L1
	{

		int size=cl*b*2; //size of L2 is twice of L1
		int offset_bits=(int)(Math.log(b)/Math.log(2));
		int block_bits=16-offset_bits;
		int cacheline2_bits=(int)(Math.log(cl*2)/Math.log(2));

		String blockmar=mar.substring(0,block_bits); //getting block number of MAR

		if(cache_L2.containsKey(blockmar))
		{
			System.out.println("#### CACHE L2 HIT ####");
			System.out.print("Address found at line number ");
			System.out.println(Integer.parseInt(cache_L2.get(blockmar),2)+" in cache L2.");
			System.out.print("Data stored at entered memory address: ");
			String offset=mar.substring(block_bits);
			int index=Integer.parseInt(cache_L2.get(blockmar)+offset,2);
			System.out.println(cachearr_L2[index]);

		}

		else
		{

			System.out.println("#### CACHE L2 MISS ####");
			String lineno=mar.substring(block_bits-cacheline2_bits,block_bits);
			String blockinmap="";
			
			for(String key:cache_L2.keySet())
			{
				if(lineno.equals(cache_L2.get(key)))
				{	
					blockinmap=key;
					break;
			}
			}
			
			
			System.out.println("Because of cache MISS in L2, block number "+Integer.parseInt(blockinmap,2)+" replaced with block number "+Integer.parseInt(blockmar,2)+" in line number "+Integer.parseInt(lineno,2)+" in cache L2.");
			
			cache_L2.remove(blockinmap);
			cache_L2.put(blockmar,lineno); //replacing block in L2 hashmap

			int [] indicesm=new int[b];
			int [] indicesc=new int[b];
			for(int i=0; i<b;i++)
			{
				String combi=String.format("%"+Integer.toString(offset_bits)+"s",Integer.toBinaryString(i));
				combi=combi.replace(' ','0');
				
				String indexm=blockmar+combi;
				indicesm[i]=Integer.parseInt(indexm,2);
				
				String indexc=lineno+combi;
				indicesc[i]=Integer.parseInt(indexc,2);
				
			}
			
			for(int j=0;j<b;j++)
			{
				cachearr_L2[indicesc[j]]=mainmemory[indicesm[j]]; //copying data into L2 array
			}
			
		}



	}

}
