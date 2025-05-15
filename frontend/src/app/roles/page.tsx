"use client"

import type React from "react"

import { useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Checkbox } from "@/components/ui/checkbox"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Eye, Pencil, Key, Trash2, RefreshCw, Search, Download, Upload, Printer, Plus } from "lucide-react"
import Link from "next/link"
import { PaginationNav } from "@/components/ui/pagination-nav"
import { fetchRoles, deleteRole, deleteMultipleRoles, exportRolesToExcel, importRoles } from "@/app/api/roles/api"
import { toast } from "@/components/ui/use-toast"
import { Toaster } from "@/components/ui/toaster" 

interface Role {
  id: number
  name: string
}


export default function RolesPage() {
  const [roles, setRoles] = useState<Role[]>([])
  const [loading, setLoading] = useState(true)
  const [searchTerm, setSearchTerm] = useState("")
  const [page, setPage] = useState(0)
  const [size, setSize] = useState(10)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [selectedRoles, setSelectedRoles] = useState<number[]>([])
  const [selectAll, setSelectAll] = useState(false)
  const [deleteLoading, setDeleteLoading] = useState(false)

  useEffect(() => {
    fetchRolesData()
  }, [page])

  // Update selectAll state when roles change
  useEffect(() => {
    if (roles.length > 0 && selectedRoles.length === roles.length) {
      setSelectAll(true)
    } else {
      setSelectAll(false)
    }
  }, [selectedRoles, roles])

  const fetchRolesData = async () => {
    setLoading(true)
    try {
      const data = await fetchRoles(page, size, searchTerm)
      setRoles(data.content)
      setTotalPages(Math.ceil(data.totalElements / size))
      setTotalElements(data.totalElements ? data.totalElements : 0)
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to load roles. Please try again. " + (error instanceof Error ? error.message : ""),
        variant: "destructive",
      })
    } finally {
      setLoading(false)
    }
  }

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault()
    setPage(0)
    fetchRolesData()
  }

  const handleRefresh = () => {
    setSearchTerm("")
    setPage(0)
    fetchRolesData()
  }

  const handleImport = async (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files.length > 0) {
      const file = e.target.files[0]
      const formData = new FormData()
      formData.append("file", file)

      try {
        await importRoles(formData)
        toast({
          title: "Success",
          description: "Roles imported successfully",
        })
        handleRefresh()
      } catch (error) {
        toast({
          title: "Error",
          description: "Failed to import roles. Please try again. " + (error instanceof Error ? error.message : ""),
          variant: "destructive",
        })
      }
    }
  }


  const handleExport = async () => {
    try {
      const blob = await exportRolesToExcel(page, size)
      
      // Create a download link for the Excel file
      const url = window.URL.createObjectURL(blob)
      const a = document.createElement("a")
      a.href = url
      a.download = "roles.xlsx"
      document.body.appendChild(a)
      a.click()
      window.URL.revokeObjectURL(url)
      document.body.removeChild(a)
      
      toast({
        title: "Success",
        description: "Roles exported successfully",
      })
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to export roles. Please try again. " + (error instanceof Error ? error.message : ""),
        variant: "destructive",
      })
    }
  }

  const handleDeleteSelected = async () => {
    if (selectedRoles.length === 0) return;

    const selectedRoleCount = selectedRoles.length;
    if (confirm(`Are you sure you want to delete ${selectedRoleCount} selected role(s)?`)) {
      setDeleteLoading(true)
      try {
        const success = await deleteMultipleRoles(selectedRoles)
        console.log(success)

        setSelectedRoles([]);
        fetchRolesData();  // Re-fetch the roles to reflect the changes
        
        toast({
          title: "Success",
          description: `${selectedRoleCount} role(s) deleted successfully`,
        })
      } catch (error) {
        toast({
          title: "Error",
          description: "Failed to delete selected roles. Please try again. " + (error instanceof Error ? error.message : ""),
          variant: "destructive"
        })
      } finally {
        setDeleteLoading(false)
      }
    }
  }

  const handleSelectRole = (id: number) => {
    if (selectedRoles.includes(id)) {
      setSelectedRoles(selectedRoles.filter((roleId) => roleId !== id))
    } else {
      setSelectedRoles([...selectedRoles, id])
    }
  }

  const handleSelectAll = () => {
    if (selectAll) {
      setSelectedRoles([])
    } else {
      setSelectedRoles(roles.map((role) => role.id))
    }
  }

  const handleDeleteRole = async (id: number) => {
    if (confirm("Are you sure you want to delete this role?")) {
      try {
        await deleteRole(id)
        
        // Remove the deleted role from the list or refresh the list
        setRoles((prevRoles) => prevRoles.filter((role) => role.id !== id))
        
        // If we deleted all roles on the current page and it's not the first page,
        // go to the previous page
        if (roles.length === 1 && page > 0) {
          setPage(page - 1)
        } else {
          fetchRolesData()
        }
        
        toast({
          title: "Success",
          description: "Role deleted successfully",
        })
      } catch (error) {
        
        toast({
          title: "Error",
          description: "Failed to delete role. Please try again. " + (error instanceof Error ? error.message : ""),
          variant: "destructive",
        })
      }
    }
  }

  return (
    <div className="container py-6">
      <div className="text-2xl font-bold mb-6 flex items-center justify-between">
        <h1>Roles</h1>
      </div>

      <div className="bg-white rounded-lg shadow-sm border overflow-hidden">
        <div className="p-4 border-b">
          <div className="flex flex-col sm:flex-row sm:items-center gap-3 justify-between">
            <form onSubmit={handleSearch} className="flex gap-2">
              <Input
                placeholder="Search roles..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-full sm:w-auto"
              />
              <Button type="submit" variant="outline" size="icon">
                <Search className="h-4 w-4" />
              </Button>
              <Button
                type="button"
                variant="outline"
                size="icon"
                onClick={handleRefresh}
                title="Refresh"
              >
                <RefreshCw className="h-4 w-4" />
              </Button>
            </form>

            <div className="flex gap-2">
              <input 
                type="file" 
                id="import-file" 
                className="hidden" 
                onChange={handleImport} 
              />
              <Button type="button"
                variant="outline"
                size="icon"
                onClick={() => document.getElementById('import-file')?.click()}
                className="flex items-center justify-center">
                <Upload className="h-4 w-4" />
              </Button>
              <Button type="button"
                variant="outline"
                size="icon"
                onClick={handleExport}
                className="flex items-center justify-center">
                <Download className="h-4 w-4" />
              </Button>
              <Button type="button"
                variant="outline"
                size="icon"
                onClick={() => window.print()}
                className="flex items-center justify-center">
                <Printer className="h-4 w-4" />
              </Button>
              <Button type="button"
                variant="outline"
                size="icon"
                asChild>
                <Link href="/roles/create" className="flex items-center gap-1">
                  <Plus className="h-4 w-4" />
                </Link>
              </Button>
            </div>
          </div>
        </div>

        <div className="overflow-x-auto h-[calc(80vh-16rem)]">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead className="w-12">
                  <Checkbox
                      checked={selectAll}
                      onCheckedChange={handleSelectAll}
                      disabled={roles.length === 0 || loading}
                  />
                </TableHead>
                <TableHead className="w-12">#</TableHead>
                <TableHead>Role Name</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody className="overflow-y-auto">
              {loading ? (
                  <TableRow>
                    <TableCell colSpan={4} className="text-center py-8">
                      <div className="flex justify-center">
                        <RefreshCw className="h-6 w-6 animate-spin" />
                      </div>
                    </TableCell>
                  </TableRow>
              ) : roles.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={4} className="text-center py-8">
                      No roles found
                    </TableCell>
                  </TableRow>
              ) : (
                  roles.map((role, index) => (
                      <TableRow key={role.id} className={selectedRoles.includes(role.id) ? "bg-muted/50" : undefined}>
                        <TableCell>
                          <Checkbox
                              checked={selectedRoles.includes(role.id)}
                              onCheckedChange={() => handleSelectRole(role.id)}
                          />
                        </TableCell>
                        <TableCell>{page * size + index + 1}</TableCell>
                        <TableCell>{role.name}</TableCell>
                        <TableCell className="text-right">
                          <div className="flex justify-end gap-2">
                            <Button variant="ghost" size="icon" asChild>
                              <Link href={`/roles/detail/${role.id}`}>
                                <Eye className="h-4 w-4" />
                              </Link>
                            </Button>
                            <Button variant="ghost" size="icon" asChild>
                              <Link href={`/roles/${role.id}/edit`}>
                                <Pencil className="h-4 w-4" />
                              </Link>
                            </Button>
                            <Button variant="ghost" size="icon" asChild>
                              <Link href={`/roles/${role.id}/permissions`}>
                                <Key className="h-4 w-4" />
                              </Link>
                            </Button>
                            <Button variant="ghost" size="icon" onClick={() => handleDeleteRole(role.id)}>
                              <Trash2 className="h-4 w-4" />
                            </Button>
                          </div>
                        </TableCell>
                      </TableRow>
                  ))
              )}
            </TableBody>
          </Table>
        </div>

        <div className="flex justify-between items-center p-4 border-t">
          <Button
              variant="destructive"
              onClick={handleDeleteSelected}
              disabled={selectedRoles.length === 0 || deleteLoading}
          >
            {deleteLoading ? (
                <RefreshCw className="h-4 w-4 mr-2 animate-spin" />
            ) : (
                <Trash2 className="h-4 w-4 mr-2" />
            )}
            Delete Selected ({selectedRoles.length})
          </Button>

          <div className="flex items-center gap-2">
            <div className="text-sm text-muted-foreground">Total Records: {totalElements}</div>
            <PaginationNav currentPage={page + 1} totalPages={totalPages} onPageChange={(p) => {
              setPage(p - 1)
            }} />
          </div>
        </div>
      </div>
      <Toaster />
    </div>
  )
}