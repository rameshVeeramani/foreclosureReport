package project.ramesh;


import org.jsoup.*;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Hello world!
 *
 */
public class App
{
    static class Property{

        public static Property Property(){
            return new Property();
        }

        String Address;
        String City;
        String Zip;
        Integer NumberOfBed;
        Integer NumberOfBath;
        String Type;
        Integer YearBuilt;
        String YearDeed;
        Boolean HasPool;
        Integer  AppraisedValue;
        String OwnerName;
        String OwnerAddress;
        String ForeClosureURl;

        private String RemoveCommas(String s){
            if (s != null){
                return s.replace(",","-|-");
            }
            return s;
        }

        @Override
        public String toString(){
            return String.format(NumberOfBed + "-" + NumberOfBath + "-" + YearBuilt);
        }

        public String toCSVString() {
            return String.format("%s,%s,%s,%d,%d,%s,%d,%s,%b,%d,%s,%s,%s",
                    RemoveCommas(Address), City, Zip, NumberOfBed, NumberOfBath, Type, YearBuilt, YearDeed, HasPool,
                    AppraisedValue, RemoveCommas(OwnerName), RemoveCommas(OwnerAddress), ForeClosureURl);
        }

}

    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );

        System.out.println(getField());

        org.jsoup.nodes.Document doc = null;

        String date = null;

        Map<String,Property> map = new HashMap<String, Property>();
        //map.put("ADDRESS","LINK");
        try{
            for (int i = 1; i < 20 ; i++) {
                doc = Jsoup.connect("https://apps.collincountytx.gov/ForeclosureNotices/Property/Index?saleDateFilter=12/4/2018&pageNumber=" + i).get();
                System.out.println(doc.title());
                Elements elements =  doc.select(".col-sm-9.col-xs-12 > div.row");

                for (Element element : elements){
                    Elements l = element.select("a[href]");

                    if (l != null) {
                       // System.out.println(l.text() + " >>> " + l.attr("href"));
                        String wkey = l.text();
                        String value = l.attr("href");
                        if ((value != null) && (value.contains("/ForeclosureNotices/Property/PropertyDetails/"))) {
                            if (key.contains(",")){key = key.replace(",",  "  ");}
                            value = "https://apps.collincountytx.gov" + value;
                            Property pd =  RetrievePropertyDetail(value);
                            map.put(key,pd);
                        }
                    }
                }
            }
            writeHashMapToCsv(map);
        }
        catch (Exception ex){
            System.out.println(ex);

        }
    }

    public static  boolean isNumeric(String s) {
        return s != null && s.matches("[-+]?\\d*\\.?\\d+");
    }

    public static Property RetrievePropertyDetail(String  furl){

        System.out.println("****** NEW *****" + furl);

        Property propertyDetail = Property.Property();
        try {
            org.jsoup.nodes.Document fcdoc = Jsoup.connect(furl).get();
            if (fcdoc != null){


                Element propertytype = fcdoc.selectFirst("#propInfo > div:nth-child(3) > span:nth-child(2)");
                System.out.println(propertytype.text());
                propertyDetail.Type = propertytype.text();

                Element a  = fcdoc.selectFirst("#propInfo > div:nth-child(2) > span:nth-child(2)");
                propertyDetail.Address = a.text();

                a  = fcdoc.selectFirst("div.row:nth-child(11) > div:nth-child(1) > div:nth-child(3) > span:nth-child(2)");
                propertyDetail.AppraisedValue = ConvertStringToInt(a.text());

                //a  = fcdoc.selectFirst("");
                propertyDetail.City = "";

                propertyDetail.ForeClosureURl = furl;

                a  = fcdoc.selectFirst("div.detailRow:nth-child(9) > span:nth-child(2)");
                boolean haspool = false;
                if  (a != null && !org.apache.commons.lang3.StringUtils.isEmpty(a.text())) {
                    haspool = a.text().trim().toLowerCase() == "n" ? false : true;
                    propertyDetail.HasPool = haspool;
                }

                a  = fcdoc.selectFirst(".container > div:nth-child(7) > div:nth-child(2) > div:nth-child(8) > span:nth-child(2)");
                propertyDetail.NumberOfBath = ConvertStringToInt(a.text());

                a  = fcdoc.selectFirst(".container > div:nth-child(7) > div:nth-child(2) > div:nth-child(7) > span:nth-child(2)");
                propertyDetail.NumberOfBed = ConvertStringToInt(a.text());

                a  = fcdoc.selectFirst(".container > div:nth-child(9) > div:nth-child(2) > div:nth-child(3) > span:nth-child(2)");
                propertyDetail.OwnerAddress = a.text();

                a  = fcdoc.selectFirst(".container > div:nth-child(9) > div:nth-child(2) > div:nth-child(2) > span:nth-child(2)");
                propertyDetail.OwnerName = a.text();

                a  = fcdoc.selectFirst(".container > div:nth-child(7) > div:nth-child(2) > div:nth-child(5) > span:nth-child(2)");
                propertyDetail.YearBuilt =ConvertStringToInt(a.text());

                propertyDetail.Zip = "";

                a  = fcdoc.selectFirst("#propInfo > div:nth-child(5) > span:nth-child(2)");
                propertyDetail.YearDeed = a.text();

            }

        } catch (Exception e) {
            System.err.println("Exception.. ");
            e.printStackTrace(); // I'd rather (re)throw it though.
        }
        return  propertyDetail;
    }

    public static int ConvertStringToInt(String s){
        int av = 0;
        if  (s != null && !org.apache.commons.lang3.StringUtils.isEmpty(s)) {
            String avmoney = s.replace("$", "").replace(",", "");
            if (avmoney != null && !org.apache.commons.lang3.StringUtils.isEmpty(avmoney) && isNumeric(avmoney) )
                av = (int) Float.parseFloat(avmoney);
        }
        return av;
    }

    public static String GetHeader(){
        List<String>  l = getField();
        String header = Arrays.toString(l.toArray());
        return header ;
    }

    public static void writeHashMapToCsv(Map<String,Property> map) throws Exception {
        System.out.println("write...");
        PrintWriter writer = new PrintWriter("collin.csv", "UTF-8");
        String header = GetHeader();
        writer.println("ADDRESS" + "," + header) ;
        try {
            Iterator it = map.entrySet().iterator();
            while (it.hasNext()) {

                Map.Entry pair = (Map.Entry)it.next();
                Property p = (Property) pair.getValue();
                System.out.println(pair.getKey() + " = " + p.toCSVString());
                writer.println(pair.getKey() + "," + p.toCSVString()) ;
                it.remove(); // avoids a ConcurrentModificationException
            }
        }
        finally {
            writer.close();
        }
    }

    public static List<String> getField()
    {
        List<String> l = new ArrayList<String>();
        Property p = Property.Property();
        Field[] fs =  p.getClass().getDeclaredFields();
        for (Field f : fs){
            System.out.println(f.getName());
            l.add(f.getName());
        }
        return  l;
    }
}
