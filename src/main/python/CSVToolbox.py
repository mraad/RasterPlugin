import arcpy, os, sys, csv

def rows_as_dicts(cursor):
    colnames = cursor.fields
    for row in cursor:
        yield dict(zip(colnames, row))

class DictUnicodeProxy(object):
    def __init__(self, d):
        self.d = d
    def __iter__(self):
        return self.d.__iter__()
    def get(self, item, default=None):
        i = self.d.get(item, default)
        if isinstance(i, unicode):
            return i.encode('utf-8')
        return i

class Toolbox(object):
    def __init__(self):
        self.label = "Export To CSV"
        self.alias = "export"
        self.tools = [ToolAttr,ToolFeat]

class ToolAttr(object):
    def __init__(self):
        self.label = "Export Attributes"
        self.description = "Export the attributes (columns) of a dataset to CSV"
        self.canRunInBackground = True
    
    def getParameterInfo(self):
        params = []
        params.append(arcpy.Parameter(name = "input", displayName = "Input dataset", direction = "Input", datatype = "Table View", parameterType = "Required"))
        params.append(arcpy.Parameter(name = "output", displayName = "Output CSV file", direction = "Output", datatype = "File", parameterType = "Required"))
        return params
    
    def isLicensed(self):
        return True
    
    def updateParameters(self, parameters):
        return
    
    def updateMessages(self, parameters):
        return
    
    def execute(self, parameters, messages):
        
        input = parameters[0].valueAsText
        output = parameters[1].valueAsText
        
        fcdescription = arcpy.Describe(input)
        fieldnames = [field.name for field in fcdescription.fields]
        
        if fcdescription.datasetType == "FeatureClass":
            arcpy.AddMessage('Excluding geometry field "{0}"'.format(fcdescription.shapeFieldName))
            fieldnames.remove(fcdescription.shapeFieldName)
        
        with open(output, 'wb') as f:
            with arcpy.da.SearchCursor(input, fieldnames) as cursor:
                writer = csv.DictWriter(f, fieldnames, restval='', extrasaction='raise', delimiter='\t')
                writer.writeheader()
                
                for row in rows_as_dicts(cursor):
                    writer.writerow(DictUnicodeProxy(row))
        f.close()
        return

class ToolFeat(object):
    def __init__(self):
        self.label = "Export Features"
        self.description = "Export the features of a dataset to CSV"
        self.canRunInBackground = True
    
    def getParameterInfo(self):
        params = []
        params.append(arcpy.Parameter(name = "input", displayName = "Input dataset", direction = "Input", datatype = "Table View", parameterType = "Required"))
        params.append(arcpy.Parameter(name = "output", displayName = "Output CSV file", direction = "Output", datatype = "File", parameterType = "Required"))
        return params
    
    def isLicensed(self):
        return True
    
    def updateParameters(self, parameters):
        return
    
    def updateMessages(self, parameters):
        return
    
    def execute(self, parameters, messages):
        
        input = parameters[0].valueAsText
        output = parameters[1].valueAsText
        
        fcdescription = arcpy.Describe(input)
        fieldnames = [field.name for field in fcdescription.fields]
        
        with open(output, 'wb') as f:
            with arcpy.da.SearchCursor(input, fieldnames) as cursor:
                writer = csv.DictWriter(f, fieldnames, restval='', extrasaction='raise', delimiter='\t')
                writer.writeheader()
                
                for row in rows_as_dicts(cursor):
                    writer.writerow(DictUnicodeProxy(row))
        f.close()
        return
