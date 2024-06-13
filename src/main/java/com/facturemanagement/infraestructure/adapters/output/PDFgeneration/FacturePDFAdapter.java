package com.facturemanagement.infraestructure.adapters.output.PDFgeneration;

import com.facturemanagement.application.ports.output.FactureGeneratePDFOutputPort;
import com.facturemanagement.domain.model.Enterprise;
import com.facturemanagement.domain.model.Facture;
import com.facturemanagement.domain.model.Product;
import com.facturemanagement.domain.model.Third;
import com.facturemanagement.infraestructure.adapters.security.IJwtUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.color.Color;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.border.Border;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.property.TextAlignment;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;

import java.awt.image.BufferedImage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Random;

public class FacturePDFAdapter implements FactureGeneratePDFOutputPort{

    @Autowired
    private IJwtUtils jwtUtils;

    private HttpRequest request = new HttpRequest();
    private Random random = new Random();

    @Override
    public byte[] generatePDF(Facture facture) {
        try{
            return generateInvoicePdf(facture);
        }catch(IOException e){
            e.printStackTrace();
            return null;
        }
    }

    private byte[] generateInvoicePdf(Facture facture) throws IOException {     
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);
        document.setFontSize(10);
        
        DateFormat dateFormat = new SimpleDateFormat("HH:mm");
        Date date = new Date();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, 24);

        Date newDate = calendar.getTime();
        DateFormat expirationDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String formattedExpirationDate = expirationDateFormat.format(newDate);

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
        
        Enterprise enterprise =  this.enterpriseData(facture);
        Third third = this.thirdData(facture);

        Image logo = null;
        try {
            BufferedImage bufferedImage = ImageIO.read(new URL(enterprise.getEntLogo()));
            ByteArrayOutputStream imageBaos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", imageBaos);
            ByteArrayInputStream imageBais = new ByteArrayInputStream(imageBaos.toByteArray());
            ImageData imageData = ImageDataFactory.create(imageBais.readAllBytes());
            logo = new Image(imageData);
            logo.setWidth(100);;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        //Informacion de empresa
        Table enterpriseData = new Table(1);
        enterpriseData.setWidthPercent(100);
        enterpriseData.addCell(new Cell().add(enterprise.getEntName()).setTextAlignment(TextAlignment.CENTER).setBorder(Border.NO_BORDER));
        enterpriseData.addCell(new Cell().add(boldText("Direccion: ",enterprise.getEntAddress())).setTextAlignment(TextAlignment.CENTER).setBorder(Border.NO_BORDER));
        enterpriseData.addCell(new Cell().add(boldText("NIT: ",enterprise.getEntNIT())).setTextAlignment(TextAlignment.CENTER).setBorder(Border.NO_BORDER));
        enterpriseData.addCell(new Cell().add(boldText("Contacto: ",enterprise.getEntContact())).setTextAlignment(TextAlignment.CENTER).setBorder(Border.NO_BORDER));
        enterpriseData.addCell(new Cell().add(new Paragraph("Factura Electrónica de compra")).setTextAlignment(TextAlignment.CENTER).setBorder(Border.NO_BORDER));
        enterpriseData.addCell(new Cell().add(boldText("Resolución de facturación aprobado por la DIAN No.","\n"+this.random.nextLong(1000000))).setTextAlignment(TextAlignment.CENTER).setBorder(Border.NO_BORDER));

        Table enterpriseDetails = new Table(2);
        enterpriseDetails.setWidthPercent(100);
        if (logo != null) {
            enterpriseDetails.addCell(new Cell().add(logo).setBorder(Border.NO_BORDER));
        }else {
            enterpriseDetails.addCell(new Cell().add("Imagen no encontrada").setBorder(Border.NO_BORDER));
        }
        enterpriseDetails.addCell(new Cell().add(enterpriseData).setBorder(Border.NO_BORDER));
        document.add(enterpriseDetails);
        document.add(new Paragraph(" "));

        //Añadir codigo CUFE
        document.add(boldText("CODIGO CUFE: ",generateCUFE()).setTextAlignment(TextAlignment.CENTER).setFontSize(8));

        // Tabla detalles de factura
        Table invoiceDetails = new Table(2);
        invoiceDetails.setWidthPercent(100);
        invoiceDetails.addCell(new Cell().add(boldText("FECHA DE EMISIÓN: ", String.valueOf(LocalDate.now()))));
        invoiceDetails.addCell(new Cell().add(boldText("AUT. NUMERACIÓN FAC: ",facture.getFactCode())));
        invoiceDetails.addCell(new Cell().add(boldText("HORA DE EMISIÓN: ",dateFormat.format(date))));
        invoiceDetails.addCell(new Cell().add(boldText("FECHA DE VENCIMIENTO: ",formattedExpirationDate)));
        document.add(invoiceDetails);

        // Tabla detalles del Emisor
        document.add(new Paragraph("DATOS DEL EMISOR").setTextAlignment(TextAlignment.CENTER).setBold());
        Table transmitterDetails = new Table(2);
        transmitterDetails.setWidthPercent(100);
        transmitterDetails.addCell(new Cell().add(boldText("NIT: ",enterprise.getEntNIT())));
        transmitterDetails.addCell(new Cell().add(boldText("CONTACTO: ",enterprise.getEntContact())));
        transmitterDetails.addCell(new Cell().add(boldText("NOMBRE: ",enterprise.getEntName())));
        transmitterDetails.addCell(new Cell().add(boldText("DIRECCION: ",enterprise.getEntAddress())));
        document.add(transmitterDetails);

        // Tabla detalles del tercero
        document.add(new Paragraph("DATOS DEL CLIENTE").setTextAlignment(TextAlignment.CENTER).setBold());
        Table clientDetails = new Table(2);
        clientDetails.setWidthPercent(100);
        clientDetails.addCell(new Cell().add(boldText("CÓDIGO DEL CLIENTE: ",third.getVerificationNumber()+"")));
        clientDetails.addCell(new Cell().add(boldText("DEPARTAMENTO: ",third.getProvince())));
        clientDetails.addCell(new Cell().add(boldText("TIPO DE DOCUMENTO: ",third.getTypeId())));
        clientDetails.addCell(new Cell().add(boldText("CIUDAD: ",third.getCity())));
        clientDetails.addCell(new Cell().add(boldText("NÚMERO DE DOCUMENTO: ",third.getIdNumber()+"")));
        clientDetails.addCell(new Cell().add(boldText("TELEFONO: ",third.getPhoneNumber())));
        clientDetails.addCell(new Cell().add(boldText("NOMBRE DE CLIENTE: ",third.getNames()+" "+third.getLastNames())));
        clientDetails.addCell(new Cell().add(boldText("CORREO: ",third.getEmail())));
        clientDetails.addCell(new Cell().add(boldText("DIRECCIÓN DEL CLIENTE: ",third.getAddress())));
        clientDetails.addCell(new Cell().add(boldText("TIPO DE PERSONA: ",third.getPersonType())));
        document.add(clientDetails);

        // tabla de productos
        document.add(new Paragraph("PRODUCTOS").setTextAlignment(TextAlignment.CENTER).setBold());
        Table itemTable = new Table(new float[]{1, 3, 1, 2, 2});
        itemTable.setWidthPercent(100);
        itemTable.addHeaderCell(new Cell().add(new Paragraph("CANTIDAD").setBold()).setBackgroundColor(Color.LIGHT_GRAY));
        itemTable.addHeaderCell(new Cell().add(new Paragraph("DESCRIPCIÓN").setBold()).setBackgroundColor(Color.LIGHT_GRAY));
        itemTable.addHeaderCell(new Cell().add(new Paragraph("IVA").setBold()).setBackgroundColor(Color.LIGHT_GRAY));
        itemTable.addHeaderCell(new Cell().add(new Paragraph("PRECIO UNITARIO").setBold()).setBackgroundColor(Color.LIGHT_GRAY));
        itemTable.addHeaderCell(new Cell().add(new Paragraph("VALOR TOTAL").setBold()).setBackgroundColor(Color.LIGHT_GRAY));

        // Productos
        for (Product p : facture.getFactProducts()) {
            itemTable.addCell(new Cell().add(new Paragraph(p.getAmount()+"")));
            itemTable.addCell(new Cell().add(new Paragraph(p.getDescription())));
            itemTable.addCell(new Cell().add(new Paragraph((p.getVat()*100)+"%")));
            itemTable.addCell(new Cell().add(new Paragraph(currencyFormat.format(p.getUnitPrice()))));
            itemTable.addCell(new Cell().add(new Paragraph(currencyFormat.format((p.getAmount()*p.getUnitPrice())))));
        }

        itemTable.addCell(new Cell().add(boldText("Cantidad Total: ",facture.getFactProducts().size()+"")));
        document.add(itemTable);
        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));

        // Cuenta total
        Table summaryTable = new Table(2);
        summaryTable.setWidthPercent(100);
        summaryTable.addCell(new Cell().add("SUBTOTALES").setBold().setBackgroundColor(Color.LIGHT_GRAY));
        summaryTable.addCell(new Cell().add(currencyFormat.format(facture.getFactSubtotals())));
        summaryTable.addCell(new Cell().add("IMPUESTO SOBRE LAS VENTAS").setBold().setBackgroundColor(Color.LIGHT_GRAY));
        summaryTable.addCell(new Cell().add(currencyFormat.format(facture.getFacSalesTax())));
        summaryTable.addCell(new Cell().add("RETFUENTE 2.5%").setBold().setBackgroundColor(Color.LIGHT_GRAY));
        summaryTable.addCell(new Cell().add(currencyFormat.format(facture.getFacWithholdingSource())));
        summaryTable.addCell(new Cell().add("TOTAL A PAGAR").setBold().setBackgroundColor(Color.LIGHT_GRAY));
        summaryTable.addCell(new Cell().add((currencyFormat.format(facture.getFactSubtotals()+facture.getFacSalesTax()+facture.getFacWithholdingSource()))));
        document.add(summaryTable);

        document.add(new Paragraph(" "));

        document.add(new Paragraph("**FACTURA GENERADA CON FINES EDUCATIVOS, NO TIENE NINGUN VALOR LEGAL**").setFontColor(Color.RED).setTextAlignment(TextAlignment.CENTER));

        document.close();

        return baos.toByteArray();
    }

    private Enterprise enterpriseData(Facture facture){
        JsonNode jsonResult = this.request.getRequest("http://contables.unicauca.edu.co/api/enterprises/enterprise/" + facture.getEntId(),jwtUtils);
        String contact = jsonResult.get("email").asText() + " - " + jsonResult.get("phone").asText();
        return new Enterprise(
            jsonResult.get("name").asText(),
            "Ficticia",
            jsonResult.get("nit").asText(),
            contact,
            jsonResult.get("logo").asText()
        );
    }

    private Third thirdData(Facture facture){
        JsonNode jsonResult = this.request.getRequest("http://contables.unicauca.edu.co/api/thirds/third?thId="+facture.getThId(),jwtUtils);
        JsonNode typeIDJson = jsonResult.get("typeId");
        return new Third(
            jsonResult.get("verificationNumber").asLong(),
            typeIDJson.get("typeId").asText(),
            jsonResult.get("idNumber").asLong(),
            jsonResult.get("names").asText(),
            jsonResult.get("lastNames").asText(),
            jsonResult.get("address").asText(),
            jsonResult.get("country").asText(),
            jsonResult.get("province").asText(),
            jsonResult.get("city").asText(),
            jsonResult.get("phoneNumber").asText(),
            jsonResult.get("email").asText(),
            jsonResult.get("personType").asText()
        );
    }

    private String generateCUFE (){
        UUID uuid = UUID.randomUUID();
        return uuid.toString().replace("-", "");
    }

    // Funcion que recibe 2 String para hacer un parrafo con uno de ellos en negrita
    private Paragraph boldText(String textToBold, String textToNormal){
        Paragraph paragraph = new Paragraph();
        Text textbold = new Text(textToBold).setBold();
        Text textNormal = new Text(textToNormal);
        paragraph.add(textbold);
        paragraph.add(textNormal);
        return paragraph;
    }
}
